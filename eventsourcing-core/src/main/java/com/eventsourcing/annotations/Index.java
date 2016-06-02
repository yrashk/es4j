/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.annotations;

import com.eventsourcing.index.IndexEngine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation informs Eventsourcing that a certain field is a CQEngine index
 * and allows configuring required index features.
 * <p>
 * <b>Please note</b>: such fields MUST be public and static to be discovered.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Index {
    IndexEngine.IndexFeature[] value() default {IndexEngine.IndexFeature.EQ};
}
