/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.symbols.ConeClassLikeLookupTag
import org.jetbrains.kotlin.fir.symbols.ConeClassifierLookupTag
import org.jetbrains.kotlin.fir.symbols.ConeClassifierLookupTagWithFixedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.utils.WeakPair

fun ConeClassifierLookupTag.toSymbol(useSiteSession: FirSession): FirClassifierSymbol<*>? =
    when (this) {
        is ConeClassLikeLookupTag -> toSymbol(useSiteSession)
        is ConeClassifierLookupTagWithFixedSymbol -> this.symbol
        else -> null
    }

@OptIn(LookupTagInternals::class)
fun ConeClassLikeLookupTag.toSymbol(useSiteSession: FirSession): FirClassLikeSymbol<*>? {
    if (this is ConeClassLookupTagWithFixedSymbol) {
        return this.symbol
    }
    val firSymbolProvider = useSiteSession.symbolProvider
    (this as? ConeClassLikeLookupTagImpl)?.boundSymbol?.takeIf { it.first === useSiteSession }?.let { return it.second }

    return firSymbolProvider.getClassLikeSymbolByClassId(classId).also {
        (this as? ConeClassLikeLookupTagImpl)?.bindSymbolToLookupTag(useSiteSession, it)
    }
}

@OptIn(LookupTagInternals::class)
fun ConeClassLikeLookupTag.toSymbolOrError(useSiteSession: FirSession): FirClassLikeSymbol<*> =
    toSymbol(useSiteSession)
        ?: error("Class symbol with classId $classId was not found")

@OptIn(LookupTagInternals::class)
fun ConeClassLikeLookupTag.toFirRegularClassSymbol(session: FirSession): FirRegularClassSymbol? =
    session.symbolProvider.getSymbolByLookupTag(this) as? FirRegularClassSymbol

@OptIn(LookupTagInternals::class)
fun ConeClassLikeLookupTagImpl.bindSymbolToLookupTag(session: FirSession, symbol: FirClassLikeSymbol<*>?) {
    boundSymbol = WeakPair(session, symbol)
}

@LookupTagInternals
fun ConeClassLikeLookupTag.toFirRegularClass(session: FirSession): FirRegularClass? =
    session.symbolProvider.getSymbolByLookupTag(this)?.fir as? FirRegularClass

fun FirSymbolProvider.getSymbolByLookupTag(lookupTag: ConeClassifierLookupTag): FirClassifierSymbol<*>? {
    return lookupTag.toSymbol(session)
}

fun FirSymbolProvider.getSymbolByLookupTag(lookupTag: ConeClassLikeLookupTag): FirClassLikeSymbol<*>? {
    return lookupTag.toSymbol(session)
}
