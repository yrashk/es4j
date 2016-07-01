/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.eventsourcing.kotlin

import com.eventsourcing.layout.ClassAnalyzer
import com.eventsourcing.layout.LayoutConstructor
import java.lang.reflect.Constructor
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaType

class KotlinParameter(val parameter : KParameter) : ClassAnalyzer.Parameter {
    override fun getName(): String {
        return parameter.name!!
    }

    override fun getType(): Class<*> {
        return parameter.type.javaType as Class<*>
    }

}
class KotlinConstructor(val constructor: KFunction<*>) : ClassAnalyzer.Constructor {

    override fun isLayoutConstructor(): Boolean {
        return constructor.annotations.find { it.javaClass.equals(LayoutConstructor::class.java) } != null
    }

    override fun getParameters(): Array<out ClassAnalyzer.Parameter> {
        return constructor.parameters.map { KotlinParameter(it) }.toTypedArray()
    }

    override fun <X : Any?> getConstructor(): Constructor<X> {
        return constructor.javaConstructor!! as Constructor<X>
    }

}
class KotlinClassAnalyzer : ClassAnalyzer {

    override fun getConstructors(klass: Class<*>?): Array<out ClassAnalyzer.Constructor> {
        return klass!!.kotlin.constructors.map { KotlinConstructor(it) }.toTypedArray()
    }

}