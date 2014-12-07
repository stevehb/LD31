package com.sblackwell.ld31.listeners;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.sblackwell.ld31.types.ContactEvent;

public class ContactSingleton implements ContactListener {
    private static ContactSingleton instance;
    public static ContactSingleton get() {
        if(instance == null) { instance = new ContactSingleton(); }
        return instance;
    }
    private ContactSingleton() {
        queue = new Array<ContactEvent>(true, 8, ContactEvent.class);
    }

    public Array<ContactEvent> queue;

    @Override
    public void beginContact(Contact contact) {
        ContactEvent event = Pools.obtain(ContactEvent.class);
        event.type = ContactEvent.ContactEventType.BEGIN;
        event.a = contact.getFixtureA().getBody();
        event.b = contact.getFixtureB().getBody();
        queue.add(event);
    }

    @Override
    public void endContact(Contact contact) {
        ContactEvent event = Pools.obtain(ContactEvent.class);
        event.type = ContactEvent.ContactEventType.END;
        event.a = contact.getFixtureA().getBody();
        event.b = contact.getFixtureB().getBody();
        queue.add(event);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) { }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) { }
}
