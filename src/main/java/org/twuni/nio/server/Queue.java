package org.twuni.nio.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Queue implements Iterable<Object> {

	private final String id;
	private final Collection<Object> items = new ArrayList<Object>();

	private int offset;

	public Queue( String id ) {
		this( id, 0 );
	}

	public Queue( String id, int offset ) {
		this.id = id;
		this.offset = offset;
	}

	public void add( Object object ) {
		offset++;
		items.add( object );
	}

	public void clear() {
		items.clear();
	}

	public int getOffset() {
		return offset;
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
			offset--;
			it.remove();
		}
	}

}
