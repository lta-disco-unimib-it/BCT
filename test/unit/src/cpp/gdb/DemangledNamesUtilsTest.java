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
package cpp.gdb;

import static org.junit.Assert.*;

import org.junit.Test;

public class DemangledNamesUtilsTest {

	@Test
	public void testSignatureNoTemplate() {
		assertEquals("Isograft::Libraries::Standard::Learn::operator()()", DemangledNamesUtils.getSignatureNoTemplate("Isograft::Libraries::Standard::Learn<Isograft::Libraries::SenderApplication::Sender_Component_State>::operator()()") );
		
		assertEquals("Isograft::Libraries::Standard::Learn::operator()()", DemangledNamesUtils.getSignatureNoTemplate("Isograft::Libraries::Standard::Learn<Isograft::Libraries::Standard::Notifier_Component_State<bool> >::operator()()") );
		
		assertEquals( "Learner::operator+(Data&)" , DemangledNamesUtils.getSignatureNoTemplate("Learner::operator+(Data&)") );
	}
	
	@Test
	public void testRemoveTemplatesFromSignature(){
		assertEquals("std::pair::~pair()",
				DemangledNamesUtils.removeTemplatesFromSignature("std::pair<std::basic_string<char,std::char_traits<char>,std::allocator<char>>,int>::~pair()"));
	
		assertEquals("std::map.run(std::string arg1,std::string arg2)",DemangledNamesUtils.removeTemplatesFromSignature("std::map.run(std::string arg1,std::string arg2)") );
		
		
		//WorkersMap::WorkersMap() 
		assertEquals ( "Data::getData():29", DemangledNamesUtils.removeTemplatesFromSignature("Data::getData():29") );
		
		
		assertEquals( "WorkersMap::getAverageSalary(std::list)",
				DemangledNamesUtils.removeTemplatesFromSignature("WorkersMap::getAverageSalary(std::list<std::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::allocator<std::basic_string<char, std::char_traits<char>, std::allocator<char> > > >)") );
	
	
		assertEquals( "Isograft::Framework::Kernel::create_configuration(std::basic_string const&)",
				DemangledNamesUtils.removeTemplatesFromSignature("Isograft::Framework::Kernel::create_configuration(std::basic_string<char,std::char_traits<char>,std::allocator<char>>const&)") );
		
		
		assertEquals( "Isograft::Map(std::basic_string,std::allocator const&)",
				DemangledNamesUtils.removeTemplatesFromSignature("Isograft::Map" +
						"<std::basic_string<char,std::char_traits<char>,std::allocator<char>>,Isograft::Framework::Configuration*,Isograft::Invalid_Key<std::basic_string<char>>>" +
						"(std::basic_string<char,std::char_traits<char>>,std::allocator<char>>const&)") );
			
	}

	
	
	@Test
	public void testRemoveNamespacesAndTemplatesFromSignature(){
	
		
		assertEquals("pair::~pair()",
				DemangledNamesUtils.removeNamespacesAndTemplatsFromSignature("std::pair<std::basic_string<char,std::char_traits<char>,std::allocator<char>>,int>::~pair()"));
	
		assertEquals("map.run(string arg1,string arg2)",DemangledNamesUtils.removeNamespacesAndTemplatsFromSignature("std::map.run(std::string arg1,std::string arg2)") );
		
		assertEquals("run(string arg1,string arg2)",DemangledNamesUtils.removeNamespacesAndTemplatsFromSignature("run(std::string arg1,std::string arg2)") );
		
		assertEquals ( "Data::getData()", DemangledNamesUtils.removeNamespacesAndTemplatsFromSignature("Data::getData()") );
		
		//WorkersMap::WorkersMap() 
		assertEquals ( "Data::getData():29", DemangledNamesUtils.removeNamespacesAndTemplatsFromSignature("Data::getData():29") );
		
		
		assertEquals( "WorkersMap::getAverageSalary(list)",
				DemangledNamesUtils.removeNamespacesAndTemplatsFromSignature("WorkersMap::getAverageSalary(std::list<std::basic_string<char, std::char_traits<char>, std::allocator<char> >, std::allocator<std::basic_string<char, std::char_traits<char>, std::allocator<char> > > >)") );
	
	
		assertEquals( "Kernel::create_configuration(basic_string const&)",
				DemangledNamesUtils.removeNamespacesAndTemplatsFromSignature("Isograft::Framework::Kernel::create_configuration(std::basic_string<char,std::char_traits<char>,std::allocator<char>>const&)") );
		
		
		assertEquals( "Isograft::Map(basic_string,allocator const&)",
				DemangledNamesUtils.removeNamespacesAndTemplatsFromSignature("Isograft::Map" +
						"<std::basic_string<char,std::char_traits<char>,std::allocator<char>>,Isograft::Framework::Configuration*,Isograft::Invalid_Key<std::basic_string<char>>>" +
						"(std::basic_string<char,std::char_traits<char>>,std::allocator<char>>const&)") );
			
	}
}
