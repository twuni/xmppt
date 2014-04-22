package org.twuni.nio.server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Queue implements Iterable<Object> {

	private final String id;
	private final Set<Object> items = new HashSet<Object>();

	public Queue( String id ) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public void add( Object object ) {
		items.add( object );
	}

	@Override
	public Iterator<Object> iterator() {
		return items.iterator();
	}

}
