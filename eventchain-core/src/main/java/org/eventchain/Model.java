package org.eventchain;

import java.util.UUID;

/**
 * A very basic Domain Model interface to be used by domain models. Although it is not
 * a requirement, this will help improving end application's composability.
 */
public interface Model {
    Repository getRepository();
    UUID id();
}
