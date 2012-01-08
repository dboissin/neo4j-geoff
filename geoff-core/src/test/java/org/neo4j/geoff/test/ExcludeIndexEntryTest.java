/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.geoff.test;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.geoff.Geoff;
import org.neo4j.geoff.Rule;
import org.neo4j.geoff.tokens.IndexToken;
import org.neo4j.geoff.tokens.NodeToken;
import org.neo4j.geoff.tokens.RelationshipToken;
import org.neo4j.geoff.tokens.Token;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.test.ImpermanentGraphDatabase;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;

public class ExcludeIndexEntryTest {

	private ImpermanentGraphDatabase db;

	@Before
	public void setUp() throws Exception {
		db = new ImpermanentGraphDatabase();
	}

	@Test
	public void testNodeIndexExclusionRule() throws Exception {
		String source = "(A)!=|People| {\"name\": \"Alice\"}";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("N'I", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof NodeToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof IndexToken);
		NodeToken nodeToken = (NodeToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.NODE, nodeToken.getTokenType());
		assertTrue(nodeToken.hasName());
		assertEquals("A", nodeToken.getName());
		IndexToken indexToken = (IndexToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.INDEX, indexToken.getTokenType());
		assertTrue(indexToken.hasName());
		assertEquals("People", indexToken.getName());
		assertTrue(rule.getData().containsKey("name"));
		assertEquals("Alice", rule.getData().get("name"));
	}

	@Test
	public void testRelationshipIndexExclusionRule() throws Exception {
		String source = "[R]!=|People| {\"name\": \"Alice\"}";
		Rule rule = Rule.from(source);
		assertNotNull(rule);
		assertEquals("R'I", rule.getDescriptor().getPattern());
		assertTrue(rule.getDescriptor().getToken(0) instanceof RelationshipToken);
		assertTrue(rule.getDescriptor().getToken(2) instanceof IndexToken);
		RelationshipToken relationshipToken = (RelationshipToken) rule.getDescriptor().getToken(0);
		assertEquals(Token.Type.REL, relationshipToken.getTokenType());
		assertTrue(relationshipToken.hasName());
		assertEquals("R", relationshipToken.getName());
		assertFalse(relationshipToken.hasType());
		IndexToken indexToken = (IndexToken) rule.getDescriptor().getToken(2);
		assertEquals(Token.Type.INDEX, indexToken.getTokenType());
		assertEquals(true, indexToken.hasName());
		assertEquals("People", indexToken.getName());
		assertTrue(rule.getData().containsKey("name"));
		assertEquals("Alice", rule.getData().get("name"));
	}

	@Test
	public void testLoadingNodeIndexExclusionRuleWithLoadParameter() throws Exception {
		// perform first call to inject node
		String source =
				"(A) {\"given-names\": \"Alice\", \"family-name\": \"Allison\"}\n" +
				"(A)<=|People| {\"name\": \"Allison, Alice\"}\n" +
				"";
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Node node = (Node) out.get("(A)");
		assertNotNull(node);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("A", node);
		// make second call to add index entry
		source = "(A)!=|People| {\"name\": \"Allison, Alice\"}";
		Geoff.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		assertTrue(db.index().existsForNodes("People"));
		Index<Node> people = db.index().forNodes("People");
		assertFalse(people.get("name", "Allison, Alice").hasNext());
	}

	@Test
	public void testLoadingRelationshipIndexExclusionRuleWithLoadParameter() throws Exception {
		// perform first call to inject relationship
		String source =
				"(A) {\"name\": \"Alice\"}\n" +
				"(B) {\"name\": \"Bob\"}\n" +
				"(A)-[R:KNOWS]->(B) {\"since\": 1977}\n" +
				"[R]<=|Friends| {\"names\": \"Alice & Bob\"}\n" +
				"";
		Map<String, PropertyContainer> out = Geoff.loadIntoNeo4j(new StringReader(source), db, null);
		assertNotNull(out);
		// build params for second call from output of first
		Relationship rel = (Relationship) out.get("[R]");
		assertNotNull(rel);
		Map<String, PropertyContainer> params = new HashMap<String, PropertyContainer>(1);
		params.put("R", rel);
		// make second call to add index entry
		source = "[R]!=|Friends| {\"names\": \"Alice & Bob\"}";
		Geoff.loadIntoNeo4j(new StringReader(source), db, params);
		// check results
		assertTrue(db.index().existsForRelationships("Friends"));
		Index<Relationship> friends = db.index().forRelationships("Friends");
		assertFalse(friends.get("names", "Alice & Bob").hasNext());
	}

}