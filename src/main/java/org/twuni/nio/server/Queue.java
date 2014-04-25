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

	public void add( Object object ) {
		items.add( object );
	}

	public void clear() {
		items.clear();
	}

	public String id() {
		return id;
	}

	@Override
	public Iterator<Object> iterator() {
		return items.iterator();
	}

	public int size() {
		return items.size();
	}

	public void transfer( Queue target ) {
		Iterator<Object> it = iterator();
		while( it.hasNext() ) {
			Object item = it.next();
			target.add( item );
			it.remove();
		}
	}

}
