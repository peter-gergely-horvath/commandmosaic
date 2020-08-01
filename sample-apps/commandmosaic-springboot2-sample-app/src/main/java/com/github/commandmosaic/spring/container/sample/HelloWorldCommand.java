/*
 * Copyright (c) 2020 Peter G. Horvath, All Rights Reserved.
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

 
package com.github.commandmosaic.spring.container.sample;

import com.github.commandmosaic.api.Command;
import com.github.commandmosaic.api.CommandContext;
import org.springframework.beans.factory.annotation.Autowired;

public class HelloWorldCommand implements Command<String> {

    @Autowired
    private MessageService messageService;

    @Override
    public String execute(CommandContext context) {
        return messageService.getMessage() + " from " + HelloWorldCommand.class.getName();
    }
}
