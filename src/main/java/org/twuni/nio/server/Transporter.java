package org.twuni.nio.server;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.twuni.Logger;

public class Transporter {

	public static class State {

		public final Map<String, Queue> pendingSend = new HashMap<String, Queue>();
		public final Map<String, Queue> pendingAcknowledgment = new HashMap<String, Queue>();

	}

	private static final Logger LOG = new Logger( Transporter.class.getName() );

	private final Map<String, Writable> targets = new HashMap<String, Writable>();
	private final State state = new State();

	public void acknowledge( String id, int count ) {

		Queue limbo = state.pendingAcknowledgment.get( id );

		if( limbo == null ) {
			return;
		}

		if( limbo.size() == count ) {
			limbo.clear();
		} else {
			Queue q = state.pendingSend.get( id );
			limbo.transfer( q );
			flush( id );
		}

	}

	public void available( Writable target, String id ) {
		targets.put( id, target );
		flush( id );
	}

	public void enqueue( Object packet, String recipient ) {

		Queue queue = state.pendingSend.get( recipient );

		if( queue == null ) {
			queue = new Queue( recipient );
			state.pendingSend.put( recipient, queue );
		}

		if( !state.pendingAcknowledgment.containsKey( recipient ) ) {
			state.pendingAcknowledgment.put( recipient, new Queue( recipient ) );
		}

		queue.add( packet );

	}

	public void flush( String recipient ) {
		Writable target = targets.get( recipient );
		if( target != null ) {
			Queue queue = state.pendingSend.get( recipient );
			Queue limbo = state.pendingAcknowledgment.get( recipient );
			if( queue != null ) {
				synchronized( queue ) {
					Iterator<Object> it = queue.iterator();
					while( it.hasNext() ) {
						Object packet = it.next();
						try {
							send( target, packet );
							if( limbo != null ) {
								limbo.add( packet );
							}
							it.remove();
						} catch( IOException exception ) {
							LOG.info( "DELAY %s", packet );
						} catch( BufferOverflowException exception ) {
							LOG.info( "DELAY %s", packet );
						}
					}
				}
			}
		}
	}

	public void restore( Collection<Queue> pendingSend, Collection<Queue> pendingAcknowledgment ) {
		for( Queue queue : pendingSend ) {
			state.pendingSend.put( queue.id(), queue );
		}
		for( Queue queue : pendingAcknowledgment ) {
			state.pendingAcknowledgment.put( queue.id(), queue );
		}
	}

	public State save() {
		return state;
	}

	public void send( Writable target, Object object ) throws IOException {
		if( target.write( object.toString().getBytes() ) < 0 ) {
			throw new IOException( "Packet not sent." );
		}
	}

	public void sent( Object packet, String recipient ) {
		Queue queue = state.pendingAcknowledgment.get( recipient );
		if( queue == null ) {
			queue = new Queue( recipient );
			state.pendingAcknowledgment.put( recipient, queue );
		}
		queue.add( packet );
	}

	public void transport( Object packet, String recipient ) {
		enqueue( packet, recipient );
		flush( recipient );
	}

	public void unavailable( String id ) {
		targets.remove( id );
	}

}
