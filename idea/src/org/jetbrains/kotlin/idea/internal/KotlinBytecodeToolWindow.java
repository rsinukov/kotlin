/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.internal;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.IdeaLogger;
import org.jetbrains.kotlin.backend.common.output.OutputFile;
import org.jetbrains.kotlin.backend.common.output.OutputFileCollection;
import org.jetbrains.kotlin.codegen.ClassBuilderFactories;
import org.jetbrains.kotlin.codegen.CompilationErrorHandler;
import org.jetbrains.kotlin.codegen.KotlinCodegenFacade;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages;
import org.jetbrains.kotlin.idea.caches.resolve.ResolutionUtils;
import org.jetbrains.kotlin.idea.debugger.DebuggerUtils;
import org.jetbrains.kotlin.idea.resolve.ResolutionFacade;
import org.jetbrains.kotlin.idea.util.InfinitePeriodicalTask;
import org.jetbrains.kotlin.idea.util.LongRunningReadTask;
import org.jetbrains.kotlin.idea.util.ProjectRootsUtil;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtScript;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.utils.StringsKt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;

public class KotlinBytecodeToolWindow extends JPanel implements Disposable {
    private final Logger LOG = Logger.getInstance(KotlinBytecodeToolWindow.class);

    private static final int UPDATE_DELAY = 1000;
    private static final String DEFAULT_TEXT = "/*\n" +
                                               "Generated bytecode for Kotlin source file.\n" +
                                               "No Kotlin source file is opened.\n" +
                                               "*/";

    public class UpdateBytecodeToolWindowTask extends LongRunningReadTask<Location, String> {
        @Override
        protected Location prepareRequestInfo() {
            if (!toolWindow.isVisible()) {
                return null;
            }

            Location location = Location.fromEditor(FileEditorManager.getInstance(myProject).getSelectedTextEditor(), myProject);
            if (location.getEditor() == null) {
                return null;
            }

            KtFile file = location.getKFile();
            if (file == null || !ProjectRootsUtil.isInProjectSource(file)) {
                return null;
            }

            return location;
        }

        @NotNull
        @Override
        protected Location cloneRequestInfo(@NotNull Location location) {
            Location newLocation = super.cloneRequestInfo(location);
            assert location.equals(newLocation) : "cloneRequestInfo should generate same location object";
            return newLocation;
        }

        @Override
        protected void hideResultOnInvalidLocation() {
            setText(DEFAULT_TEXT);
        }

        @NotNull
        @Override
        protected String processRequest(@NotNull Location location) {
            KtFile jetFile = location.getKFile();
            assert jetFile != null;

            return getBytecodeForFile(jetFile, enableInline.isSelected(), enableAssertions.isSelected(), enableOptimization.isSelected());
        }

        @Override
        protected void onResultReady(@NotNull Location requestInfo, String resultText) {
            Editor editor = requestInfo.getEditor();
            assert editor != null;

            if (resultText == null) {
                return;
            }

            setText(resultText);

            int fileStartOffset = requestInfo.getStartOffset();
            int fileEndOffset = requestInfo.getEndOffset();

            Document document = editor.getDocument();
            int startLine = document.getLineNumber(fileStartOffset);
            int endLine = document.getLineNumber(fileEndOffset);
            if (endLine > startLine && fileEndOffset > 0 && document.getCharsSequence().charAt(fileEndOffset - 1) == '\n') {
                endLine--;
            }

            Document byteCodeDocument = myEditor.getDocument();

            Pair<Integer, Integer> linesRange = mapLines(byteCodeDocument.getText(), startLine, endLine);
            int endSelectionLineIndex = Math.min(linesRange.second + 1, byteCodeDocument.getLineCount());

            int startOffset = byteCodeDocument.getLineStartOffset(linesRange.first);
            int endOffset = Math.min(byteCodeDocument.getLineStartOffset(endSelectionLineIndex), byteCodeDocument.getTextLength());

            myEditor.getCaretModel().moveToOffset(endOffset);
            myEditor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
            myEditor.getCaretModel().moveToOffset(startOffset);
            myEditor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);

            myEditor.getSelectionModel().setSelection(startOffset, endOffset);
        }
    }

    private final Editor myEditor;
    private final Project myProject;
    private final ToolWindow toolWindow;
    private final JCheckBox enableInline;
    private final JCheckBox enableOptimization;
    private final JCheckBox enableAssertions;
    private final JButton decompile;

    public KotlinBytecodeToolWindow(final Project project, ToolWindow toolWindow) {
        super(new BorderLayout());
        myProject = project;
        this.toolWindow = toolWindow;

        myEditor = EditorFactory.getInstance().createEditor(
                EditorFactory.getInstance().createDocument(""), project, JavaFileType.INSTANCE, true);
        add(myEditor.getComponent());

        JPanel optionPanel = new JPanel(new FlowLayout());
        add(optionPanel, BorderLayout.NORTH);

        decompile = new JButton("Decompile");
        if (KotlinDecompilerService.Companion.getInstance() != null) {
            optionPanel.add(decompile);
            decompile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Location location = Location.fromEditor(FileEditorManager.getInstance(myProject).getSelectedTextEditor(), myProject);
                    KtFile file = location.getKFile();
                    if (file != null) {
                        try {
                            KotlinDecompilerAdapterKt.showDecompiledCode(file);
                        }
                        catch (IdeaLogger.InternalException ex) {
                            LOG.info(ex);
                            Messages.showErrorDialog(myProject, "Failed to decompile " + file.getName() + ": " + ex, "Kotlin Bytecode Decompiler");
                        }
                    }
                }
            });
        }

        /*TODO: try to extract default parameter from compiler options*/
        enableInline = new JCheckBox("Inline", true);
        enableOptimization = new JCheckBox("Optimization", true);
        enableAssertions = new JCheckBox("Assertions", true);
        optionPanel.add(enableInline);
        optionPanel.add(enableOptimization);
        optionPanel.add(enableAssertions);

        new InfinitePeriodicalTask(UPDATE_DELAY, Alarm.ThreadToUse.SWING_THREAD, this, new Computable<LongRunningReadTask>() {
            @Override
            public LongRunningReadTask compute() {
                return new UpdateBytecodeToolWindowTask();
            }
        }).start();

        setText(DEFAULT_TEXT);
    }

    // public for tests
    @NotNull
    public static String getBytecodeForFile(
            final KtFile jetFile,
            boolean enableInline,
            boolean enableAssertions,
            boolean enableOptimization
    ) {
        GenerationState state;
        try {
            state = compileSingleFile(jetFile, enableInline, enableAssertions, enableOptimization);
        }
        catch (ProcessCanceledException e) {
            throw e;
        }
        catch (Exception e) {
            return printStackTraceToString(e);
        }

        StringBuilder answer = new StringBuilder();

        Collection<Diagnostic> diagnostics = state.getCollectedExtraJvmDiagnostics().all();
        if (!diagnostics.isEmpty()) {
            answer.append("// Backend Errors: \n");
            answer.append("// ================\n");
            for (Diagnostic diagnostic : diagnostics) {
                answer.append("// Error at ")
                        .append(diagnostic.getPsiFile().getName())
                        .append(StringsKt.join(diagnostic.getTextRanges(), ","))
                        .append(": ")
                        .append(DefaultErrorMessages.render(diagnostic))
                        .append("\n");
            }
            answer.append("// ================\n\n");
        }

        OutputFileCollection outputFiles = state.getFactory();
        for (OutputFile outputFile : outputFiles.asList()) {
            answer.append("// ================");
            answer.append(outputFile.getRelativePath());
            answer.append(" =================\n");
            answer.append(outputFile.asText()).append("\n\n");
        }

        return answer.toString();
    }

    @NotNull
    public static GenerationState compileSingleFile(
            final KtFile ktFile,
            boolean enableInline,
            boolean enableAssertions,
            boolean enableOptimization
    ) {
        ResolutionFacade resolutionFacade = ResolutionUtils.getResolutionFacade(ktFile);

        BindingContext bindingContextForFile = resolutionFacade.analyzeFullyAndGetResult(Collections.singletonList(ktFile)).getBindingContext();

        kotlin.Pair<BindingContext, List<KtFile>> result = DebuggerUtils.INSTANCE.analyzeInlinedFunctions(
                resolutionFacade, bindingContextForFile, ktFile, !enableInline
        );

        BindingContext bindingContext = result.getFirst();
        List<KtFile> toProcess = result.getSecond();

        GenerationState.GenerateClassFilter generateClassFilter = new GenerationState.GenerateClassFilter() {

            @Override
            public boolean shouldGeneratePackagePart(@NotNull KtFile file) {
                return file == ktFile;
            }

            @Override
            public boolean shouldAnnotateClass(@NotNull KtClassOrObject processingClassOrObject) {
                return true;
            }

            @Override
            public boolean shouldGenerateClass(@NotNull KtClassOrObject processingClassOrObject) {
                return processingClassOrObject.getContainingKtFile() == ktFile;
            }

            @Override
            public boolean shouldGenerateScript(@NotNull KtScript script) {
                return script.getContainingKtFile() == ktFile;
            }
        };

        GenerationState state = new GenerationState(ktFile.getProject(), ClassBuilderFactories.TEST,
                                    resolutionFacade.getModuleDescriptor(), bindingContext,
                                    toProcess,
                                    !enableAssertions,
                                    !enableAssertions,
                                    generateClassFilter,
                                    !enableInline,
                                    !enableOptimization,
                                    /*useTypeTableInSerializer=*/false);
        KotlinCodegenFacade.compileCorrectFiles(state, CompilationErrorHandler.THROW_EXCEPTION);
        return state;
    }

    private static Pair<Integer, Integer> mapLines(String text, int startLine, int endLine) {
        int byteCodeLine = 0;
        int byteCodeStartLine = -1;
        int byteCodeEndLine = -1;

        List<Integer> lines = new ArrayList<Integer>();
        for (String line : text.split("\n")) {
            line = line.trim();

            if (line.startsWith("LINENUMBER")) {
                int ktLineNum = new Scanner(line.substring("LINENUMBER".length())).nextInt() - 1;
                lines.add(ktLineNum);
            }
        }
        Collections.sort(lines);

        for (Integer line : lines) {
            if (line >= startLine) {
                startLine = line;
                break;
            }
        }

        for (String line : text.split("\n")) {
            line = line.trim();

            if (line.startsWith("LINENUMBER")) {
                int ktLineNum = new Scanner(line.substring("LINENUMBER".length())).nextInt() - 1;

                if (byteCodeStartLine < 0 && ktLineNum == startLine) {
                    byteCodeStartLine = byteCodeLine;
                }

                if (byteCodeStartLine > 0 && ktLineNum > endLine) {
                    byteCodeEndLine = byteCodeLine - 1;
                    break;
                }
            }

            if (byteCodeStartLine >= 0 && (line.startsWith("MAXSTACK") || line.startsWith("LOCALVARIABLE") || line.isEmpty())) {
                byteCodeEndLine = byteCodeLine - 1;
                break;
            }


            byteCodeLine++;
        }

        if (byteCodeStartLine == -1 || byteCodeEndLine == -1) {
            return new Pair<Integer, Integer>(0, 0);
        }
        else {
            return new Pair<Integer, Integer>(byteCodeStartLine, byteCodeEndLine);
        }
    }

    private static String printStackTraceToString(Throwable e) {
        StringWriter out = new StringWriter(1024);
        PrintWriter printWriter = new PrintWriter(out);
        try {
            e.printStackTrace(printWriter);
            return out.toString().replace("\r", "");
        }
        finally {
            printWriter.close();
        }
    }

    private void setText(@NotNull final String resultText) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                myEditor.getDocument().setText(StringUtil.convertLineSeparators(resultText));
            }
        });
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(myEditor);
    }
}
