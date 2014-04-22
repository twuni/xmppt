package org.twuni.nio.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import java.util.TreeSet;

public class DispatcherPool implements DispatcherProvider {

	public static final int DEFAULT_SIZE = 5;

	static class Envelope implements Comparable<Envelope> {

		public final Dispatcher dispatcher;
		public final int id;
		public int load;
		public boolean running;
		private final String label;

		public Envelope( int id, Dispatcher dispatcher ) {
			this.id = id;
			this.dispatcher = dispatcher;
			this.load = 0;
			this.running = false;
			this.label = String.format( "%s %d", Envelope.class.getSimpleName(), Integer.valueOf( id ) );
		}

		@Override
		public int compareTo( Envelope envelope ) {
			return envelope != null ? load < envelope.load ? -1 : load > envelope.load ? 1 : 0 : -1;
		}

		@Override
		public String toString() {
			return label;
		}

	}

	private final Set<Envelope> pool = new TreeSet<Envelope>();

	public DispatcherPool( int size, EventHandler eventHandler ) throws IOException {
		this( size, SelectorProvider.provider().openSelector(), eventHandler );
	}

	public DispatcherPool( EventHandler eventHandler ) throws IOException {
		this( DEFAULT_SIZE, SelectorProvider.provider().openSelector(), eventHandler );
	}

	public DispatcherPool( Selector selector, EventHandler eventHandler ) {
		this( DEFAULT_SIZE, selector, eventHandler );
	}

	public DispatcherPool( int size, Selector selector, EventHandler eventHandler ) {
		for( int i = 0; i < size; i++ ) {
			pool.add( new Envelope( i, new Dispatcher( selector, eventHandler ) ) );
		}
	}

	@Override
	public Dispatcher provideDispatcher() {

		Envelope best = null;

		for( Envelope envelope : pool ) {
			if( best == null || best.compareTo( envelope ) > 0 ) {
				best = envelope;
			}
		}

		if( best != null ) {
			if( !best.running ) {
				new Thread( best.dispatcher, best.toString() ).start();
				best.running = true;
			}
			best.load++;
			return best.dispatcher;
		}

		return null;

	}

}
