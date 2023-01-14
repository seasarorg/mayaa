/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.engine.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;

/**
 * @author katochin
 */
public class WriteProcessorTest {

	private WriteProcessor _writeProcessor;

	@BeforeEach
	public void setUp() throws Exception {
		_writeProcessor = new WriteProcessor();
	}
	
	public void testBodyTextMatches() {
		assertEquals(true, _writeProcessor.isExistsBodyTextInScript("${ (new Number(bodyText))*2 \n}"));
		assertEquals(false, _writeProcessor.isExistsBodyTextInScript(""));
		assertEquals(true, _writeProcessor.isExistsBodyTextInScript("${'123'+bodyText\n} and ${bodyText+'321'\n}"));
		assertEquals(false, _writeProcessor.isExistsBodyTextInScript("dummy"));
	}
	
}
