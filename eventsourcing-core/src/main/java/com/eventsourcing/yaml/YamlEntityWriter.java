/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.yaml;

import com.eventsourcing.Entity;
import com.eventsourcing.EntityWriter;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

public class YamlEntityWriter implements EntityWriter {

    private final Yaml yaml;
    private final YamlRepresenter representer;

    public YamlEntityWriter() {
        representer = new YamlRepresenter();
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(representer, dumperOptions);
    }

    @SneakyThrows
    @Override public void write(OutputStream stream, Iterator<Entity> iterator) throws IOException {
        yaml.dumpAll(iterator, new OutputStreamWriter(stream));
    }

    @Override public void writeLayouts(OutputStream stream) throws IOException {
        yaml.dumpAll(representer.getLayouts().iterator(), new OutputStreamWriter(stream));
        representer.getLayouts().clear();
    }
}
