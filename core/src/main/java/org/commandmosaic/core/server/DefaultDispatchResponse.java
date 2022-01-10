/*
 * Copyright (c) 2020-2022 Peter G. Horvath, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commandmosaic.core.server;

import org.commandmosaic.api.server.DispatchResponse;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class DefaultDispatchResponse implements DispatchResponse {

    private final Supplier<OutputStream> outputStreamSupplier;
    private final Supplier<OutputStream> errorStreamSupplier;

    private List<FailureListener> failureListenersList;

    public DefaultDispatchResponse(Supplier<OutputStream> streamSupplier) {
        this(streamSupplier, streamSupplier);
    }

    public DefaultDispatchResponse(Supplier<OutputStream> outputStreamSupplier,
                                   Supplier<OutputStream> errorStreamSupplier) {
        this.outputStreamSupplier = outputStreamSupplier;
        this.errorStreamSupplier = errorStreamSupplier;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStreamSupplier.get();
    }

    @Override
    public OutputStream getErrorStream() {
        return errorStreamSupplier.get();
    }

    @Override
    public void notifyErrorListeners(Throwable throwable) {
        if (failureListenersList != null) {
            for(FailureListener failureListener : failureListenersList) {
                failureListener.onFailure(throwable);
            }
        }

    }

    @Override
    public void addListener(FailureListener failureListener) {
        if (failureListenersList == null) {
            failureListenersList = new LinkedList<>();
        }

        failureListenersList.add(failureListener);
    }
}
