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

package com.griddynamics.qa.sprimber.engine.processor.cucumber;

import gherkin.pickles.Pickle;
import gherkin.pickles.PickleTag;
import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fparamonov
 */
class TagFilter {

    private List<String> targetTagFilters;
    private List<Expression> expressions = new ArrayList<>();

    TagFilter(List<String> targetTagFilters) {
        this.targetTagFilters = targetTagFilters;
        initExpressions();
    }

    private void initExpressions() {
        TagExpressionParser tagExpressionParser = new TagExpressionParser();
        expressions.addAll(targetTagFilters.stream().map(tagExpressionParser::parse).collect(Collectors.toList()));
    }

    boolean filter(Pickle pickle) {
        return expressions.stream().allMatch(expression -> expression.evaluate(getTagsFromPickle(pickle)));
    }

    private List<String> getTagsFromPickle(Pickle pickle) {
        return pickle.getTags().stream().map(PickleTag::getName).collect(Collectors.toList());
    }
}