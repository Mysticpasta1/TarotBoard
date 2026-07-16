package com.mystic.tarotboard.utils;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hides pieces that an identical piece is sitting exactly on top of.
 *
 * <p>A full deck is one card per suit/value pair plus the wilds — over four thousand
 * panes, of a dozen nodes each — and they spend most of the game stacked on a single
 * spot. JavaFX has no occlusion culling of its own: every one of those cards is walked,
 * painted and hit-tested, even though only the top one of a pile is ever on screen. That
 * is what makes a tablet crawl, and what makes a card dragged across the deck stutter,
 * since the drag dirties the region the whole deck lives in.</p>
 *
 * <p>Two cards count as one when their translation and rotation match exactly. Cards are
 * all the same size, so the upper one covers the lower to the pixel — including the
 * transparent rounded corners, which are cut identically on both — and hiding the lower
 * one cannot change what is drawn. Exact equality is the right test rather than a
 * tolerance: piles are formed by code that assigns the same coordinates (the deal, the
 * reshuffle, the split, the drop snap), while two cards a hair apart genuinely do show
 * two cards. Rotation is part of the match because a turned card's corners stick out
 * past the one beneath it.</p>
 *
 * <p>Hidden panes drop out of picking as well as painting, which is what keeps this
 * invisible to the player: only the top of a pile was ever reachable by a click, and
 * that one is the one left visible.</p>
 */
public final class OcclusionCuller {
    /** Marks the panes this culler is allowed to hide. */
    private static final String CULLABLE = "tb_cullable";

    private final Pane container;
    /** Set while a pass is already queued, so a batch move schedules one pass, not thousands. */
    private boolean pending;

    /**
     * @param container the pane the tracked pieces are children of; its child order is
     *                  the z-order the culler reads to decide which piece is on top
     */
    public OcclusionCuller(Pane container) {
        this.container = container;
    }

    /**
     * Places a piece under the culler's control and schedules a pass.
     *
     * <p>The piece is watched rather than re-registered by each caller that moves it: a
     * card is moved by the deal, drags, the discard zone, saves, and by messages from
     * every peer, and a pass missed at any one of those would leave a pile showing a card
     * that is no longer on top.</p>
     */
    public void track(StackPane pane) {
        // Re-theming rewires a card's interactions on the pane it already had, so tracking
        // has to be idempotent or every theme change would leave another set of listeners
        // on every card in the deck.
        if (Boolean.TRUE.equals(pane.getProperties().get(CULLABLE))) return;
        pane.getProperties().put(CULLABLE, Boolean.TRUE);
        InvalidationListener onMove = obs -> markDirty();
        pane.translateXProperty().addListener(onMove);
        pane.translateYProperty().addListener(onMove);
        pane.rotateProperty().addListener(onMove);
        // A discarded piece leaves the board; whatever it was covering has to come back.
        pane.parentProperty().addListener(onMove);
        markDirty();
    }

    /**
     * Requests a pass on the next tick. Coalescing is what makes this affordable: a
     * reshuffle moves every card in the deck, and culling on each individual move would
     * be quadratic in the size of the deck.
     */
    public void markDirty() {
        if (pending) return;
        pending = true;
        Platform.runLater(this::cull);
    }

    private void cull() {
        pending = false;
        List<Node> children = container.getChildren();
        Set<Spot> occupied = new HashSet<>(children.size() * 2);
        // Front to back: the first piece found on a spot is the one on top of it.
        for (int i = children.size() - 1; i >= 0; i--) {
            Node node = children.get(i);
            if (!Boolean.TRUE.equals(node.getProperties().get(CULLABLE))) continue;
            boolean covered = !occupied.add(new Spot(node.getTranslateX(), node.getTranslateY(), node.getRotate()));
            node.setVisible(!covered);
        }
    }

    /** A place on the board a piece can be, precise enough that one piece hides another. */
    private record Spot(double x, double y, double rotate) {
    }
}
