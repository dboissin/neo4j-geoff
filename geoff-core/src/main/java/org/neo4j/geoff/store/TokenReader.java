/**
 * Copyright (c) 2002-2012 "Neo Technology,"
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
package org.neo4j.geoff.store;

import org.neo4j.geoff.except.SyntaxError;
import org.neo4j.geoff.util.UeberReader;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class TokenReader extends UeberReader {

	public TokenReader(Reader reader) {
		super(reader);
	}

	public static boolean isDigit(char ch) {
		return Character.isDigit(ch);
	}

	public static boolean isNameChar(char ch) {
		return Character.isLetterOrDigit(ch) || ch == '_';
	}

	public Token[] readTokens() throws IOException, SyntaxError {
		ArrayList<Token> tokens = new ArrayList<Token>(20);
		boolean done = false;
		while(!done) {
			try {
				char ch = peek();
				switch(ch) {
				case '(':
					tokens.add(readNodeToken());
					break;
				case '[':
					tokens.add(readRelationshipToken());
					break;
				case '|':
					tokens.add(readIndexToken());
					break;
				case '-':
					read('-');
					tokens.add(new Token(Token.Type.SINGLE_LINE));
					break;
				case '>':
					read('>');
					tokens.add(new Token(Token.Type.ARROW));
					break;
				case '<':
					read('<');
					read('=');
					tokens.add(new Token(Token.Type.INCLUDED_IN));
					break;
				case '!':
					read('!');
					if(peek() == '=') {
						read('=');
						tokens.add(new Token(Token.Type.EXCLUDED_FROM));
					} else {
						tokens.add(new Token(Token.Type.BANG));
					}
					break;
				case '=':
					read('=');
					tokens.add(new Token(Token.Type.DOUBLE_LINE));
					break;
				default:
					throw new UnexpectedCharacterException(ch);
				}
			} catch (EndOfStreamException e) {
				done = true;
			} catch (UnexpectedCharacterException e) {
				throw new SyntaxError("Unexpected character encountered in token", e);
			}
		}
		return tokens.toArray(new Token[tokens.size()]);
	}

	public NodeToken readNodeToken() throws IOException, EndOfStreamException, UnexpectedCharacterException {
		read('(');
		NodeToken token;
		String name = readName();
		token = new NodeToken(name);
		read(')');
		return token;
	}

	public RelationshipToken readRelationshipToken() throws IOException, EndOfStreamException, UnexpectedCharacterException {
		read('[');
		String name = readName();
		String type = "";
		if(peek() == ':') {
			read(':');
			type = readName();
		}
		read(']');
		return new RelationshipToken(name, type);
	}

	public Token readIndexToken() throws IOException, EndOfStreamException, UnexpectedCharacterException {
		read('|');
		String name = readName();
		read('|');
		return new IndexToken(name);
	}

	public String readName() throws IOException, EndOfStreamException, UnexpectedCharacterException {
		StringBuilder str = new StringBuilder(80);
		while (isNameChar(peek())) {
			str.append(read());
		}
		if (peek() == '.') {
			str.append(read('.'));
			char ch = peek();
			if (ch >= '1' && ch <= '9') {
				str.append(read(ch));
			} else {
				throw new UnexpectedCharacterException(ch);
			}
			while (isDigit(peek())) {
				str.append(read());
			}
		}
		return str.toString();
	}

}