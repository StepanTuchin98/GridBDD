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

package com.griddynamics.qa.sprimber.runtime;

import com.griddynamics.qa.sprimber.discovery.TestSuiteDiscovery;
import com.griddynamics.qa.sprimber.engine.Node;
import com.griddynamics.qa.sprimber.stepdefinition.StepClassAnnotationsProvider;
import com.griddynamics.qa.sprimber.stepdefinition.TestMethodsBulkLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class used to instantiate {@link ExecutionContext} with custom pre-initialised runtime objects
 *
 * @author fparamonov
 */

@Slf4j
@RequiredArgsConstructor
public class ExecutionContextFactory extends AbstractFactoryBean<ExecutionContext> {

    private final ApplicationContext applicationContext;
    private final TestMethodsBulkLoader bulkLoader;
    private final List<StepClassAnnotationsProvider> stepClassAnnotationsProviders;
    private final List<TestSuiteDiscovery> testSuiteDiscoveries;

    @Override
    public Class<?> getObjectType() {
        return ExecutionContext.class;
    }

    @Override
    protected ExecutionContext createInstance() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ExecutionContext executionContext = new ExecutionContext();
        bulkLoader.load(getMethodCandidates());
        executionContext.getNodes().addAll(exploreNodes());
        TestSuiteDiscovery.Statistic discoveryInfo = joinStatistic();
        executionContext.getStatistic().putAll(discoveryInfo);
        logTotalDiscoveryResults(discoveryInfo);
        stopWatch.stop();
        log.debug("Initial setup took: '{}' seconds", stopWatch.getTotalTimeSeconds());
        return executionContext;
    }

    private Stream<Method> getMethodCandidates() {
        Map<String, Object> targetBeans = new HashMap<>();
        List<Class<? extends Annotation>> allMarkerAnnotations = stepClassAnnotationsProviders.stream()
                .map(StepClassAnnotationsProvider::provide)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        allMarkerAnnotations.forEach(aClass -> targetBeans.putAll(applicationContext.getBeansWithAnnotation(aClass)));
        return targetBeans.values().stream()
                .flatMap(bean -> Arrays.stream(bean.getClass().getDeclaredMethods()));
    }

    private List<Node> exploreNodes() {
        return testSuiteDiscoveries.stream()
                .map(this::discoverRootNodeAndLog)
                .collect(Collectors.toList());
    }

    private Node discoverRootNodeAndLog(TestSuiteDiscovery discovery) {
        log.debug("Started test discovery from '{}'", discovery.name());
        Node rootNode = discovery.discover();
        logDiscoveryResults(discovery.getDiscoveredInfo());
        log.debug("Finished discovery");
        return rootNode;
    }

    private TestSuiteDiscovery.Statistic joinStatistic() {
        return testSuiteDiscoveries.stream()
                .map(TestSuiteDiscovery::getDiscoveredInfo)
                .collect(TestSuiteDiscovery.Statistic::new, TestSuiteDiscovery.Statistic::accumulate, HashMap::putAll);
    }

    private void logTotalDiscoveryResults(TestSuiteDiscovery.Statistic statistic) {
        log.info("Total discovery statistic:");
        statistic.forEach((key, value) -> log.info("\tStage: '{}' count: '{}'", key, value));
        log.info("Initial test discovery Completed");
    }

    private void logDiscoveryResults(TestSuiteDiscovery.Statistic statistic) {
        log.debug("Current discovery statistic:");
        statistic.forEach((key, value) -> log.debug("\tStage: '{}' count: '{}'", key, value));
        log.debug("Initial test discovery Completed");
    }
}
