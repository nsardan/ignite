/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.ignite.internal.pagemem.backup;

import java.nio.ByteBuffer;
import java.util.Collection;
import org.apache.ignite.internal.GridDirectCollection;
import org.apache.ignite.plugin.extensions.communication.Message;
import org.apache.ignite.plugin.extensions.communication.MessageCollectionItemType;
import org.apache.ignite.plugin.extensions.communication.MessageReader;
import org.apache.ignite.plugin.extensions.communication.MessageWriter;

public class RestoreBackupMessage implements Message {
    /** */
    private static final long serialVersionUID = 0L;

    private long backupId;

    @GridDirectCollection(String.class)
    private Collection<String> cacheNames;

    public RestoreBackupMessage() {
    }

    public RestoreBackupMessage(long backupId, Collection<String> cacheNames) {
        this.backupId = backupId;
        this.cacheNames = cacheNames;
    }

    public long backupId() {
        return backupId;
    }

    public Collection<String> cacheNames() {
        return cacheNames;
    }

    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        writer.setBuffer(buf);

        if (!writer.isHeaderWritten()) {
            if (!writer.writeHeader(directType(), fieldsCount()))
                return false;

            writer.onHeaderWritten();
        }

        switch (writer.state()) {
            case 0:
                if (!writer.writeLong("backupId", backupId))
                    return false;

                writer.incrementState();

            case 1:
                if (!writer.writeCollection("cacheNames", cacheNames, MessageCollectionItemType.STRING))
                    return false;

                writer.incrementState();

        }

        return true;
    }

    @Override public boolean readFrom(ByteBuffer buf, MessageReader reader) {
        reader.setBuffer(buf);

        if (!reader.beforeMessageRead())
            return false;

        switch (reader.state()) {
            case 0:
                backupId = reader.readLong("backupId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 1:
                cacheNames = reader.readCollection("cacheNames", MessageCollectionItemType.STRING);

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return reader.afterMessageRead(RestoreBackupMessage.class);
    }

    @Override public byte directType() {
        return -28;
    }

    @Override public byte fieldsCount() {
        return 2;
    }

    @Override public void onAckReceived() {
        // No-op
    }
}
