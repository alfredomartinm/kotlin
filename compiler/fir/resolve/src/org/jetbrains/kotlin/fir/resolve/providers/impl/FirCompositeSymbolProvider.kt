/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.providers.impl

import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProviderInternals
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.symbols.CallableId
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult

class FirCompositeSymbolProvider(val providers: List<FirSymbolProvider>) : FirSymbolProvider() {
    private val cache = Cache()

    override fun getClassLikeSymbolByFqName(classId: ClassId): FirClassLikeSymbol<*>? {
        return cache.classCache.getOrPut(classId) {
            providers.firstNotNullResult { it.getClassLikeSymbolByFqName(classId) }
        }
    }

    @OptIn(ExperimentalStdlibApi::class, FirSymbolProviderInternals::class)
    override fun getTopLevelCallableSymbols(packageFqName: FqName, name: Name): List<FirCallableSymbol<*>> {
        return cache.topLevelCallableSymbolsCache.getOrPut(CallableId(packageFqName, null, name)) {
            buildList {
                providers.forEach { it.getTopLevelCallableSymbolsTo(this, packageFqName, name) }
            }
        }
    }

    @FirSymbolProviderInternals
    override fun getTopLevelCallableSymbolsTo(destination: MutableList<FirCallableSymbol<*>>, packageFqName: FqName, name: Name) {
        error("Should not be called")
    }

    override fun getNestedClassifierScope(classId: ClassId): FirScope? {
        return providers.firstNotNullResult { it.getNestedClassifierScope(classId) }
    }

    override fun getPackage(fqName: FqName): FqName? {
        return cache.packageCache.getOrPut(fqName) {
            providers.firstNotNullResult { it.getPackage(fqName) }
        }
    }

    override fun getAllCallableNamesInPackage(fqName: FqName): Set<Name> {
        return providers.flatMapTo(mutableSetOf()) { it.getAllCallableNamesInPackage(fqName) }
    }

    override fun getClassNamesInPackage(fqName: FqName): Set<Name> {
        return providers.flatMapTo(mutableSetOf()) { it.getClassNamesInPackage(fqName) }
    }

    private class Cache {
        val packageCache: MutableMap<FqName, FqName?> = mutableMapOf()
        val classCache: MutableMap<ClassId, FirClassLikeSymbol<*>?> = mutableMapOf()
        val topLevelCallableSymbolsCache: MutableMap<CallableId, List<FirCallableSymbol<*>>> = mutableMapOf()
    }
}
