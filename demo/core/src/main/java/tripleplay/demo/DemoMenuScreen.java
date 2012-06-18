//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import playn.core.PlayN;

import react.UnitSlot;

import tripleplay.game.Screen;
import tripleplay.game.ScreenStack;
import tripleplay.game.UIScreen;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Shim;
import tripleplay.ui.SimpleStyles;
import tripleplay.ui.Style;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.TableLayout;

import tripleplay.demo.anim.*;
import tripleplay.demo.game.*;
import tripleplay.demo.particle.*;
import tripleplay.demo.ui.*;

/**
 * Displays a top-level menu of our various demo screens.
 */
public class DemoMenuScreen extends UIScreen
{
    public DemoMenuScreen (ScreenStack stack) {
        _stack = stack;
        _screens = new DemoScreen[] {
            // tripleplay.ui
            new MiscDemo(), new LabelDemo(), new SliderDemo(),
            new BackgroundDemo(), new LayoutDemo(), new BorderLayoutDemo(),
            new FlowLayoutDemo(), null, null,
            // tripleplay.anim
            new FramesDemo(), new RepeatDemo(), null,
            // tripleplay.game
            new ScreensDemo(stack), null, null,
            // tripleplay.particle
            new FountainDemo(), new FireworksDemo(), null,
        };
    }

    @Override public void wasShown () {
        super.wasShown();
        _root = iface.createRoot(AxisLayout.vertical().gap(15), SimpleStyles.newSheet(), layer);
        _root.addStyles(Style.BACKGROUND.is(Background.bordered(0xFFCCCCCC, 0xFF99CCFF, 5).
                                            inset(5, 10)));
        _root.setSize(width(), height());

        _root.add(new Label("Triple Play Demos").addStyles(Style.FONT.is(DemoScreen.TITLE_FONT)));

        Group grid = new Group(new TableLayout(
                                   TableLayout.COL.alignRight(),
                                   TableLayout.COL.stretch(),
                                   TableLayout.COL.stretch(),
                                   TableLayout.COL.stretch()).gaps(10, 10));
        _root.add(grid);

        for (int ii = 0; ii < _screens.length; ii++) {
            if (ii%3 == 0) grid.add(new Label(_rlabels[ii/3]));
            final DemoScreen screen = _screens[ii];
            if (screen == null) {
                grid.add(new Shim(1, 1));
            } else {
                Button button = new Button(screen.name());
                final int ss = ii;
                button.clicked().connect(new UnitSlot() { public void onEmit () {
                    _stack.push(screen);
                    screen.back.clicked().connect(new UnitSlot() { public void onEmit () {
                        _stack.remove(screen);
                    }});
                }});
                grid.add(button);
            }
        }
    }

    @Override public void wasHidden () {
        super.wasHidden();
        iface.destroyRoot(_root);
    }

    protected Button screen (String title, final ScreenFactory factory) {
        Button button = new Button(title);
        button.clicked().connect(new UnitSlot() { public void onEmit () {
            final DemoScreen screen = factory.apply();
            _stack.push(screen);
            screen.back.clicked().connect(new UnitSlot() { public void onEmit () {
                _stack.remove(screen);
            }});
        }});
        return button;
    }

    protected interface ScreenFactory {
        DemoScreen apply ();
    }

    protected final String[] _rlabels = {
        "tripleplay.ui", "", "",
        "tripleplay.anim",
        "tripleplay.game",
        "tripleplay.particle",
    };
    protected final DemoScreen[] _screens;

    protected final ScreenStack _stack;
    protected Root _root;
}