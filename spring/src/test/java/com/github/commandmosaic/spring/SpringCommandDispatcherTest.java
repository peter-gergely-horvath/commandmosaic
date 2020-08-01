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

 
package com.github.commandmosaic.spring;

import com.github.commandmosaic.api.Command;
import com.github.commandmosaic.api.CommandDispatcher;
import com.github.commandmosaic.api.Parameter;
import com.github.commandmosaic.api.CommandContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = TestConfig.class)
@EnableTransactionManagement
@EnableConfigurationProperties
//@EnableAspectJAutoProxy(proxyTargetClass = true)
public class SpringCommandDispatcherTest {

    @Autowired
    private CommandDispatcher commandDispatcher;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Test
    public void testNumberDispatcher() {

        Object result;
        result = commandDispatcher.dispatchCommand("GetNumberCommand",
                Collections.<String, Object>singletonMap("number", 42), null);

        Assert.assertEquals("The number is: 42", result);

        result = commandDispatcher.dispatchCommand("GetNumberCommand",
                Collections.<String, Object>singletonMap("number", 123), null);

        Assert.assertEquals("The number is: 123", result);
    }

    @Test
    public void testTransactionalCommandDispatch() {

        String commandName = getClass().getSimpleName() + "$" + TransactionActiveTestCommand.class.getSimpleName();

        assertNoTransactionIsProgress(platformTransactionManager);

        Integer isolationLevel = (Integer) commandDispatcher.dispatchCommand(commandName,
                Collections.singletonMap("expectedIsolationLevel", Isolation.REPEATABLE_READ), null);

        Assert.assertEquals((Object)TransactionDefinition.ISOLATION_REPEATABLE_READ, isolationLevel);

        assertNoTransactionIsProgress(platformTransactionManager);
    }

    @Test
    public void testNonTransactionalCommandDispatch() {

        String commandName = getClass().getSimpleName() + "$" + TransactionNotActiveTestCommand.class.getSimpleName();

        assertNoTransactionIsProgress(platformTransactionManager);

        Integer isolationLevel = (Integer) commandDispatcher.dispatchCommand(commandName,null, null);

        Assert.assertEquals(null, isolationLevel);

        assertNoTransactionIsProgress(platformTransactionManager);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public static class TransactionActiveTestCommand implements Command<Integer> {

        @Autowired
        private PlatformTransactionManager platformTransactionManager;

        @Parameter
        private Isolation expectedIsolationLevel;

        @Override
        public Integer execute(CommandContext context) {

            assertTransactionIsInProgress(platformTransactionManager);

            final Integer currentTransactionIsolationLevel =
                    TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();

            Assert.assertEquals((Object) expectedIsolationLevel.value(), currentTransactionIsolationLevel);

            return currentTransactionIsolationLevel;
        }
    }

    public static class TransactionNotActiveTestCommand implements Command<Integer> {

        @Autowired
        private PlatformTransactionManager platformTransactionManager;

        @Override
        public Integer execute(CommandContext context) {

            assertNoTransactionIsProgress(platformTransactionManager);

            final Integer currentTransactionIsolationLevel =
                    TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();

            return currentTransactionIsolationLevel;
        }
    }

    private static void assertTransactionIsInProgress(PlatformTransactionManager platformTransactionManager) {
        DefaultTransactionDefinition td = new DefaultTransactionDefinition();
        td.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_MANDATORY);

        TransactionStatus transaction = platformTransactionManager.getTransaction(td);
        Assert.assertNotNull(transaction);
    }

    private static void assertNoTransactionIsProgress(PlatformTransactionManager platformTransactionManager) {
        DefaultTransactionDefinition td = new DefaultTransactionDefinition();
        td.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_NOT_SUPPORTED);

        TransactionStatus transaction = platformTransactionManager.getTransaction(td);
        Assert.assertNotNull(transaction);
    }
}
