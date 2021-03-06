/*
 * Copyright 2017 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.server.dashboard;

import com.thoughtworks.go.config.security.Permissions;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class GoDashboardPipelineGroup {
    private String name;
    private Permissions permissions;
    private Map<String, GoDashboardPipeline> pipelines = new LinkedHashMap<>();

    public GoDashboardPipelineGroup(String name, Permissions permissions) {
        this.name = name;
        this.permissions = permissions;
    }


    public void addPipeline(GoDashboardPipeline goDashboardPipeline) {
        if (goDashboardPipeline != null) {
            pipelines.put(goDashboardPipeline.name().toString(), goDashboardPipeline);
        }
    }

    public String etag() {
        try {
            MessageDigest digest = DigestUtils.getSha256Digest();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new DigestOutputStream(new NullOutputStream(), digest));
            outputStreamWriter.write(name);
            outputStreamWriter.write("/");
            outputStreamWriter.write(Integer.toString(permissions.hashCode()));
            outputStreamWriter.write("[");

            for (Map.Entry<String, GoDashboardPipeline> entry : pipelines.entrySet()) {
                long lastUpdatedTimeStamp = entry.getValue().getLastUpdatedTimeStamp();
                outputStreamWriter.write(entry.getKey());
                outputStreamWriter.write(":");
                outputStreamWriter.write(Long.toString(lastUpdatedTimeStamp));
            }

            outputStreamWriter.write("]");
            outputStreamWriter.flush();

            return Hex.encodeHexString(digest.digest());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public String getName() {
        return name;
    }

    public Collection<GoDashboardPipeline> allPipelines() {
        return pipelines.values();
    }

    public Set<String> allPipelineNames() {
        return pipelines.keySet();
    }

    public boolean canBeAdministeredBy(String userName) {
        return permissions.admins().contains(userName);
    }

    public boolean canBeViewedBy(String userName) {
        return permissions.viewers().contains(userName);
    }

    public boolean hasPermissions() {
        return permissions != null;
    }

    public boolean hasPipelines() {
        return !pipelines.isEmpty();
    }
}
