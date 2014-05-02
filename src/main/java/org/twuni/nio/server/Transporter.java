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

	}

	private static final Logger LOG = new Logger( Transporter.class.getName() );

	private final Map<String, Writable> targets = new HashMap<String, Writable>();
	private final State state = new State();

	public void available( Writable target, String targetID ) {
		available( target, targetID, null );
	}

	public void available( Writable target, String targetID, Queue sent ) {
		targets.put( targetID, target );
		flush( targetID, sent );
	}

	private void enqueue( Object packet, String targetID ) {

		Queue queue = state.pendingSend.get( targetID );

		if( queue == null ) {
			queue = new Queue( targetID );
			state.pendingSend.put( targetID, queue );
		}

		queue.add( packet );

	}

	public void flush( String targetID ) {
		flush( targetID, null );
	}

	public void flush( String targetID, Queue sent ) {
		Writable target = targets.get( targetID );
		if( target != null ) {
			Queue queue = state.pendingSend.get( targetID );
			if( queue != null ) {
				synchronized( queue ) {
					Iterator<Object> it = queue.iterator();
					while( it.hasNext() ) {
						Object packet = it.next();
						try {
							send( target, packet );
							if( sent != null ) {
								sent.add( packet );
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

	private void restore( Collection<Queue> pendingSend ) {
		for( Queue queue : pendingSend ) {
			state.pendingSend.put( queue.id(), queue );
		}
	}

	public void restore( State state ) {
		restore( state.pendingSend.values() );
	}

	public State save() {
		return state;
	}

	private void send( Writable target, Object object ) throws IOException {
		if( target.write( object.toString().getBytes() ) < 0 ) {
			throw new IOException( "Packet not sent." );
		}
	}

	public void transport( Object packet, String targetID ) {
		transport( packet, targetID, null );
	}

	public void transport( Object packet, String targetID, Queue sent ) {
		enqueue( packet, targetID );
		flush( targetID, sent );
	}

	public void unavailable( String targetID ) {
		targets.remove( targetID );
	}

}
