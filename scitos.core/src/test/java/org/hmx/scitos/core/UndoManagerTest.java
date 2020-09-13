package org.hmx.scitos.core;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.hmx.scitos.domain.IModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test of the {@link UndoManager} class.
 */
public class UndoManagerTest {

    /** The generic managedModel's initial state (as reference). */
    private static TestModelImpl MODEL_CLONE;
    /** The generic managedModel instance to use in the undo-manager. */
    private TestModelImpl managedModel;
    /** Undo-manager for the managedModel to test. */
    private UndoManager<TestModelImpl> undoManager;

    /** Initial setup: create a reference test model. */
    @BeforeClass
    public static void setUp() {
        UndoManagerTest.MODEL_CLONE = new TestModelImpl(0);
    }

    /**
     * Preparation for each test: create a new test implementation of {@link IModel} and an {@link UndoManager} for it.
     */
    @Before
    public void prepareManager() {
        this.managedModel = UndoManagerTest.MODEL_CLONE.clone();
        this.undoManager = new UndoManager<>(this.managedModel);
        this.undoManager.setLimit(2);
    }

    /** Test: of manager's canUndo method. */
    @Test(expected = IllegalStateException.class)
    public void testCanUndo() {
        Assert.assertFalse(this.undoManager.canUndo());
        this.undoManager.undoableEditHappened(this.managedModel);
        Assert.assertTrue(this.undoManager.canUndo());
        this.undoManager.undo();
        Assert.assertFalse(this.undoManager.canUndo());
        this.undoManager.undo();
    }

    /** Test: of manager's canRedo method. */
    @Test(expected = IllegalStateException.class)
    public void testCanRedo() {
        Assert.assertFalse(this.undoManager.canRedo());
        this.undoManager.undoableEditHappened(this.managedModel);
        Assert.assertFalse(this.undoManager.canRedo());
        this.undoManager.undo();
        Assert.assertTrue(this.undoManager.canRedo());
        this.undoManager.redo();
        Assert.assertFalse(this.undoManager.canRedo());
        this.undoManager.redo();
    }

    /** Test: of manager's undoableEditHappened method. */
    @Test
    public void testUndoableEditHappened() {
        Assert.assertFalse(this.undoManager.canUndo());
        Assert.assertFalse(this.undoManager.canRedo());
        this.undoManager.undoableEditHappened(this.managedModel);
        Assert.assertTrue(this.undoManager.canUndo());
        Assert.assertFalse(this.undoManager.canRedo());
    }

    /** Test: of manager's undo method. */
    @Test
    public void testUndo() {
        this.managedModel.changeState();
        this.undoManager.undoableEditHappened(this.managedModel);
        final TestModelImpl undoResult = this.undoManager.undo();
        Assert.assertNotEquals(this.managedModel, undoResult);
        Assert.assertEquals(UndoManagerTest.MODEL_CLONE, undoResult);
    }

    /** Test: of multiple iterations of manager's undo method. */
    @Test
    public void testUndoMultipleTimes() {
        final int iterationCount = 3;
        this.undoManager.setLimit(iterationCount);
        final List<TestModelImpl> oldStates = new LinkedList<>();
        for (int iteration = 0; iteration < iterationCount; iteration++) {
            oldStates.add(this.managedModel.clone());
            this.managedModel.changeState();
            this.undoManager.undoableEditHappened(this.managedModel);
        }
        final Deque<TestModelImpl> undoStates = new LinkedList<>();
        for (int iteration = 0; iteration < iterationCount; iteration++) {
            undoStates.addFirst(this.undoManager.undo());
        }
        Assert.assertEquals(oldStates, undoStates);
    }

    /** Test: of manager's redo method. */
    @Test
    public void testRedo() {
        this.managedModel.changeState();
        this.undoManager.undoableEditHappened(this.managedModel);
        this.undoManager.undo();
        final TestModelImpl redoResult = this.undoManager.redo();
        Assert.assertNotSame(this.managedModel, redoResult);
        Assert.assertEquals(this.managedModel, redoResult);
    }

    /** Test: of multiple iterations of manager's redo method. */
    @Test
    public void testRedoMultipleTimes() {
        final int iterationCount = 3;
        this.undoManager.setLimit(iterationCount);
        final List<TestModelImpl> originalStates = new LinkedList<>();
        for (int iteration = 0; iteration < iterationCount; iteration++) {
            this.managedModel.changeState();
            originalStates.add(this.managedModel.clone());
            this.undoManager.undoableEditHappened(this.managedModel);
        }
        for (int iteration = 0; iteration < iterationCount; iteration++) {
            this.undoManager.undo();
        }
        final List<TestModelImpl> redoStates = new LinkedList<>();
        for (int iteration = 0; iteration < iterationCount; iteration++) {
            redoStates.add(this.undoManager.redo());
        }
        Assert.assertEquals(originalStates, redoStates);
    }

    /** Test: of reset method. */
    @Test
    public void testReset() {
        this.undoManager.undoableEditHappened(this.managedModel);
        this.undoManager.undoableEditHappened(this.managedModel);
        this.undoManager.undo();
        Assert.assertTrue(this.undoManager.canUndo());
        Assert.assertTrue(this.undoManager.canRedo());
        this.undoManager.reset(this.managedModel);
        Assert.assertFalse(this.undoManager.canUndo());
        Assert.assertFalse(this.undoManager.canRedo());
        this.managedModel.changeState();
        this.undoManager.undoableEditHappened(this.managedModel);
        Assert.assertEquals(UndoManagerTest.MODEL_CLONE, this.undoManager.undo());
    }

    /** Test: of setLimit method. */
    @Test
    public void testLimit() {
        final int limit = 2;
        final int undoableEdits = limit + 2;
        this.undoManager.setLimit(limit);
        for (int iteration = 0; iteration < undoableEdits; iteration++) {
            this.undoManager.undoableEditHappened(this.managedModel);
        }
        for (int iteration = 0; iteration < limit; iteration++) {
            this.undoManager.undo();
        }
        Assert.assertFalse(this.undoManager.canUndo());
    }

    /** Test: of setLimit method to deactivate the undo-manager. */
    @Test
    public void testLimitZero() {
        this.undoManager.setLimit(0);
        this.undoManager.undoableEditHappened(this.managedModel);
        Assert.assertFalse(this.undoManager.canUndo());
    }

    private static class TestModelImpl implements IModel<TestModelImpl> {

        /** Internal state value to differentiate between changed models. */
        private int state;

        /**
         * Constructor.
         *
         * @param initialState
         *            internal state value to initialize
         */
        TestModelImpl(final int initialState) {
            this.state = initialState;
        }

        /** Generic method to change managedModel's internal state. */
        void changeState() {
            this.state++;
        }

        @Override
        public int hashCode() {
            return this.state;
        }

        @Override
        public boolean equals(final Object otherObject) {
            if (this == otherObject) {
                return true;
            }
            if (!(otherObject instanceof TestModelImpl)) {
                return false;
            }
            return this.state == ((TestModelImpl) otherObject).state;
        }

        @Override
        public TestModelImpl clone() {
            return new TestModelImpl(this.state);
        }
    }
}
