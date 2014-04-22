package org.twuni.nio.server;

import java.nio.BufferOverflowException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.twuni.xmppt.xmpp.core.Message;

public class Transporter {
	
	private static final Logger LOG = new Logger( Transporter.class.getName() );

	private final Map<String, Writable> targets = new HashMap<String, Writable>();
	private final Map<String, Queue> waiting = new HashMap<String, Queue>();

	public void restore( Collection<Queue> queues ) {
		for( Queue queue : queues ) {
			waiting.put( queue.id(), queue );
		}
	}

	public Collection<Queue> save() {
		return waiting.values();
	}

	public void available( Writable target, String id ) {
		targets.put( id, target );
		flush( id );
	}

	public void unavailable( String id ) {
		targets.remove( id );
	}

	public void enqueue( Message message ) {
		String recipient = message.to();
		Queue queue = waiting.get( recipient );
		if( queue == null ) {
			queue = new Queue( recipient );
			waiting.put( recipient, queue );
		}
		queue.add( message );
	}

	public void transport( Message message ) {
		enqueue( message );
		flush( message.to() );
	}

	public void flush( String recipient ) {
		Writable target = targets.get( recipient );
		if( target != null ) {
			Queue queue = waiting.get( recipient );
			if( queue != null ) {
				for( Iterator<Object> it = queue.iterator(); it.hasNext(); ) {
					Object message = it.next();
					try {
						send( target, message );
						it.remove();
					} catch( BufferOverflowException exception ) {
						LOG.info( "DELAY %s", message );
					}
				}
			}
		}
	}

	public void send( Writable target, Object object ) {
		target.write( object.toString().getBytes() );
	}

}
