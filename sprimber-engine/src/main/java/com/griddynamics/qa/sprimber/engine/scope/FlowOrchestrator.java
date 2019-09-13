/*
Copyright (c) 2010-2018 Grid Dynamics International, Inc. All Rights Reserved
http://www.griddynamics.com

This library is free software; you can redistribute it and/or modify it under the terms of
the GNU Lesser General Public License as published by the Free Software Foundation; either
version 2.1 of the License, or any later version.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

$Id:
@Project:     Sprimber
@Description: Framework that provide bdd engine and bridges for most popular BDD frameworks
 */

package com.griddynamics.qa.sprimber.engine.scope;

import com.griddynamics.qa.sprimber.event.SprimberEventPublisher;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Kind of event listener that monitor test case start and finish events.
 * Based on this events test case context cleaned and removed or created
 *
 * @author fparamonov
 */

@Component
public class FlowOrchestrator {

    private final AbstractBeanFactory beanFactory;

    public FlowOrchestrator(AbstractBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @EventListener
    public void setupTestCaseContext(SprimberEventPublisher.TestStartedEvent testStartedEvent) {
        TestCaseContextHolder.setupNewContext(testStartedEvent.getTestDefinition().getRuntimeId());
    }

    @EventListener
    public void resetTestCaseContextOld(SprimberEventPublisher.TestFinishedEvent testFinishedEvent) {
        TestCaseContextHolder.cleanContext(beanFactory);
    }
}