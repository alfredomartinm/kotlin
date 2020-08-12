/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.providers.impl

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.dependenciesWithoutSelf
import org.jetbrains.kotlin.fir.resolve.providers.AbstractFirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.firSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProviderInternals
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.symbols.CallableId
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult

open class FirDependenciesSymbolProviderImpl(val session: FirSession) : AbstractFirSymbolProvider<FirClassLikeSymbol<*>>() {
    protected open val dependencyProviders by lazy {
        val moduleInfo = session.moduleInfo ?: return@lazy emptyList()
        moduleInfo.dependenciesWithoutSelf().mapNotNull {
            session.sessionProvider?.getSession(it)?.firSymbolProvider
        }.toList()
    }

    @FirSymbolProviderInternals
    override fun getTopLevelCallableSymbolsTo(destination: MutableList<FirCallableSymbol<*>>, packageFqName: FqName, name: Name) {
        dependencyProviders.flatMapTo(destination) { provider -> provider.getTopLevelCallableSymbols(packageFqName, name) }
    }

    override fun getNestedClassifierScope(classId: ClassId): FirScope? {
        return dependencyProviders.firstNotNullResult { it.getNestedClassifierScope(classId) }
    }

    override fun getClassLikeSymbolByFqName(classId: ClassId): FirClassLikeSymbol<*>? {
        for (provider in dependencyProviders) {
            provider.getClassLikeSymbolByFqName(classId)?.let {
                return it
            }
        }
        return null
    }

    override fun getPackage(fqName: FqName): FqName? {
        for (provider in dependencyProviders) {
            provider.getPackage(fqName)?.let {
                return it
            }
        }
        return null
    }

    override fun getAllCallableNamesInPackage(fqName: FqName): Set<Name> {
        return dependencyProviders.flatMapTo(mutableSetOf()) { it.getAllCallableNamesInPackage(fqName) }
    }

    override fun getClassNamesInPackage(fqName: FqName): Set<Name> {
        return dependencyProviders.flatMapTo(mutableSetOf()) { it.getClassNamesInPackage(fqName) }
    }
}
