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

package com.griddynamics.qa.sprimber.discovery;

import com.griddynamics.qa.sprimber.configuration.SprimberProperties;
import com.griddynamics.qa.sprimber.engine.Node;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleTag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.griddynamics.qa.sprimber.engine.Node.*;

/**
 * @author fparamonov
 */

@RequiredArgsConstructor
class CucumberSuiteDiscovery implements TestSuiteDiscovery {

    private final Compiler compiler;
    private final TokenMatcher tokenMatcher;
    private final CucumberTestBinder cucumberTestBinder;
    private final SprimberProperties sprimberProperties;
    private final Parser<GherkinDocument> gherkinParser;
    private final ApplicationContext applicationContext;
    private final CucumberTagFilter tagFilter;

    @Override
    public Node discover() {
        Node testSuite = Node.createRootNode("testSuite", BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE | BYPASS_CHILDREN_AFTER_ITERATION_ERROR);
        featureResourcesStream()
                .map(this::buildCucumberDocument)
                .forEach(cucumberDocument -> testCaseNodeDiscover(testSuite, cucumberDocument));
        return testSuite;
    }

    private void testCaseNodeDiscover(Node parentNode, CucumberDocument cucumberDocument) {
        Builder builder = new Builder()
                .withDescription(cucumberDocument.getDocument().getFeature().getDescription())
                .withName(cucumberDocument.getDocument().getFeature().getName())
                .withRole("testCase")
                .withSubNodeModes(BYPASS_BEFORE_WHEN_BYPASS_MODE | BYPASS_AFTER_WHEN_BYPASS_MODE | BYPASS_CHILDREN_AFTER_ITERATION_ERROR);
        Node testCase = parentNode.addChild(builder);
        compiler.compile(cucumberDocument.getDocument()).stream()
                .filter(pickleTagFilter())
                .forEach(pickle -> cucumberTestBinder.buildAndAddTestNode(testCase, pickle, cucumberDocument));
    }

    private Predicate<Pickle> pickleTagFilter() {
        return pickle -> tagFilter.filter(getTagsFromPickle(pickle));
    }

    private List<String> getTagsFromPickle(Pickle pickle) {
        return pickle.getTags().stream()
                .map(PickleTag::getName)
                .collect(Collectors.toList());
    }

    private Stream<Resource> featureResourcesStream() {
        try {
            return Arrays.stream(applicationContext.getResources(sprimberProperties.getFeaturePath()));
        } catch (IOException e) {
            // TODO: 2019-09-10 handle the exception from resource unavailability correctly
            throw new RuntimeException(String.format("Could not find the resources by this path: %s", sprimberProperties.getFeaturePath()));
        }
    }

    private CucumberDocument buildCucumberDocument(Resource resource) {
        try {
            GherkinDocument document = gherkinParser.parse(new InputStreamReader(resource.getInputStream()), tokenMatcher);
            CucumberDocument cucumberDocument = new CucumberDocument();
            cucumberDocument.setDocument(document);
            cucumberDocument.setUrl(resource.getURL());
            return cucumberDocument;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    class CucumberDocument {

        private GherkinDocument document;
        private URL url;

        GherkinDocument getDocument() {
            return document;
        }

        void setDocument(GherkinDocument document) {
            this.document = document;
        }

        URL getUrl() {
            return url;
        }

        void setUrl(URL url) {
            this.url = url;
        }
    }
}
