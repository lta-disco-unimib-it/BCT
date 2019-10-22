/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani
 *   
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package util.cbe;

public interface CBEExtendedDataElements {
	String STACK_TRACE = "StackTrace";
	String VIOLATION = "Violation";
	String VIOLATED_MODEL = "ViolatedModel";
	String CURRENT_ACTIONS = "CurrentActions";
	String CURRENT_TESTS = "CurrentTests";
	String PARAMETERS_STATES = "ParametersStates";
	String VIOLATION_TYPE = "ViolationType";
	String CURRENT_STATES = "CurrentStates";
	String VIOLATED_MODEL_TYPE = "ViolatedModelType";
	String FAILING_TEST_ID = "FailingTestId";
	String FAILING_TEST_NAME = "FailingTestName";
	String FAILING_ACTION_ID = "FailingActionId";
	String CATCHING_METHOD = "CatchingMethod";
	String EXCEPTION_CLASS = "ExceptionClass";
	String EXCEPTION_MSG = "ExceptionMSG";
	String FAILURE_CLASS = "FailureClass";
	String ANOMALOUS_CALL_SEQUENCE = "AnomlaousCallSequence";
	String DESTINATION_STATE = "DestinationState";
	String UNEXPECTED_SEQUENCE = "UnexpectedSequence";
	Object ANOMALOUS_EVENT_POSITION = "AnomalousEventPosition";
}