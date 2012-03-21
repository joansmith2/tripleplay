//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.Point;

import playn.core.Game;
import playn.core.GroupLayer;
import playn.core.Keyboard;
import playn.core.Layer;
import playn.core.Pointer;

import react.Value;

/**
 * The main class that integrates the Triple Play UI with a PlayN game. This class is mainly
 * necessary to handle the proper dispatching of input events to UI elements. Create an interface
 * instance, create {@link Root} groups via the interface and add the {@link Root#layer}s into your
 * scene graph wherever you desire. Call {@link #update} and {@link #paint} from {@link
 * Game#update} and {@link Game#paint} to render your interface.
 */
public class Interface
{
    /**
     * This listener must be configured in order for keyboard events to be dispatched to focused
     * {@link Element}s.
     */
    public final Keyboard.Listener klistener = new Keyboard.Listener() {
        @Override public void onKeyDown (Keyboard.Event event) {
            if (_focused.get() == null) return;
            _focused.get().onKeyDown(event);
        }
        @Override public void onKeyTyped (Keyboard.TypedEvent event) {
            if (_focused.get() == null) return;
            _focused.get().onKeyTyped(event);
        }
        @Override public void onKeyUp (Keyboard.Event event) {
            if (_focused.get() == null) return;
            _focused.get().onKeyUp(event);
        }
    };

    /**
     * Posts a runnable that will be executed after the next time the interface is validated.
     * Processing deferred actions is not tremendously efficient, so don't call this every frame.
     */
    public void deferAction (Runnable action) {
        _actions.add(action);
    }

    /**
     * Updates the elements in this interface. Must be called from {@link Game#update}.
     */
    public void update (float delta) {
        // nada at the moment
    }

    /**
     * "Paints" the elements in this interface. Must be called from {@link Game#update}.
     */
    public void paint (float alpha) {
        for (int ii = 0, ll = _roots.size(); ii < ll; ii++) {
            _roots.get(ii).validate();
        }

        // run any deferred actions
        if (!_actions.isEmpty()) {
            List<Runnable> actions = new ArrayList<Runnable>(_actions);
            _actions.clear();
            for (Runnable action : actions) {
                try {
                    action.run();
                } catch (Exception e) {
                    Log.log.warning("Interface action failed: " + action, e);
                }
            }
        }
    }

    /**
     * Clears out the current focused component, if any.
     */
    public void clearFocus () {
        _focused.update(null);
    }

    /**
     * Creates a root element with the specified layout and stylesheet.
     */
    public Root createRoot (Layout layout, Stylesheet sheet) {
        Root root = new Root(this, layout, sheet);
        _roots.add(root);
        return root;
    }


    /**
     * Creates a root element with the specified layout and stylesheet and adds its layer to the
     * specified parent.
     */
    public Root createRoot (Layout layout, Stylesheet sheet, GroupLayer parent) {
        Root root = createRoot(layout, sheet);
        parent.add(root.layer);
        return root;
    }

    /**
     * Removes the supplied root element from this interface. If the root's layer has a parent, the
     * layer will be removed from the parent as well. This leaves the Root's layer in existence, so
     * it may be used again. If you're done with the Root and all of the elements inside of it, call
     * destroyRoot to free its resources.
     */
    public void removeRoot (Root root) {
        _roots.remove(root);
        if (root.layer.parent() != null) root.layer.parent().remove(root.layer);
    }

    /**
     * Removes the supplied root element from this interface and destroys its layer. Destroying the
     * layer destroys the layers of all elements contained in the root as well. Use this method if
     * you're done with the Root. If you'd like to reuse it, call removeRoot instead.
     */
    public void destroyRoot (Root root) {
        _roots.remove(root);
        root.layer.destroy();
    }

    protected final Value<Keyboard.Listener> _focused = Value.create(null);

    protected final List<Root> _roots = new ArrayList<Root>();
    protected final List<Root> _dispatch = new ArrayList<Root>();
    protected final List<Runnable> _actions = new ArrayList<Runnable>();
}
