/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing

import java.util.*

/**
 * A very basic Domain Model interface to be used by domain models. Although it is not
 * a requirement, this will help improving end application's composability.
 */
interface Model {
    val repository: Repository
    val id: UUID
}