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

 
package org.commandmosaic.spring.container.sample;


import org.commandmosaic.api.CommandDispatcher;
import org.commandmosaic.api.configuration.CommandDispatcherConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandDispatcherConfiguration springCommandDispatcherConfiguration() {
		return CommandDispatcherConfiguration.builder()
				.rootPackageFromClass(DemoApplication.class)
				.build();
	}


	@Service
	static class HelloWorldMessageService implements MessageService {

		@Override
		public String getMessage() {
			return "Hello Spring world";
		}
	}

	@Service
	static class SampleInvocation implements CommandLineRunner {

		private final CommandDispatcher commandDispatcher;

		@Autowired
		public SampleInvocation(CommandDispatcher commandDispatcher) {
			this.commandDispatcher = commandDispatcher;
		}

		@Override
		public void run(String... strings) throws Exception {

			Object message = commandDispatcher.dispatchCommand(HelloWorldCommand.class, null, null);
			System.out.println(message);
		}
	}

}