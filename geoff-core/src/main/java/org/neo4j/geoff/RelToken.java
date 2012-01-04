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
package org.neo4j.geoff;

public class RelToken extends NameableToken {

	protected final String type;

	public RelToken(String name, String type) {
		super(Type.REL, name);
		this.type = type;
	}

	public RelToken(String name) {
		super(Type.REL, name);
		this.type = null;
	}

	public boolean hasType() {
		return !(this.type == null || this.type.isEmpty() || "*".equals(this.type));
	}

	public String getType() {
		return this.type;
	}

	@Override
	public String getFullName() {
		return String.format("[%s]", this.name);
	}

	@Override
	public String toString() {
		if(this.type == null || this.type.isEmpty()) {
			return String.format("[%s]", this.name);
		} else {
			return String.format("[%s:%s]", this.name, this.type);
		}
	}

}
