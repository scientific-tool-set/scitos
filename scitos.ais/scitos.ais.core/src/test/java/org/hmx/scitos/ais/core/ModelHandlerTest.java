/*
   Copyright (C) 2016 HermeneutiX.org

   This file is part of SciToS.

   SciToS is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   SciToS is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with SciToS. If not, see <http://www.gnu.org/licenses/>.
 */

package org.hmx.scitos.ais.core;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.KeyStroke;

import org.hmx.scitos.ais.domain.model.AisProject;
import org.hmx.scitos.ais.domain.model.DetailCategory;
import org.hmx.scitos.ais.domain.model.Interview;
import org.hmx.scitos.ais.domain.model.MutableDetailCategoryModel;
import org.hmx.scitos.ais.domain.model.TextToken;
import org.hmx.scitos.core.HmxException;
import org.hmx.scitos.domain.util.CollectionUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test of the {@link ModelHandlerImpl} class.
 */
public class ModelHandlerTest {

    private static MutableDetailCategoryModel categoryModel;
    private AisProject project;
    private ModelHandlerImpl modelHandler;
    private Interview interview;
    private TextToken paragraphStartToken;

    /**
     * Initial setup: get the default category model from {@link AisOption#createDefaultCategoryModel()}.
     */
    @BeforeClass
    public static void setUp() {
        ModelHandlerTest.categoryModel = AisOption.createDefaultCategoryModel();
    }

    /** Final clearing up: discard the reference to the default category model. */
    @AfterClass
    public static void tearDown() {
        ModelHandlerTest.categoryModel = null;
    }

    /**
     * Preparation for each test: create a new handled project, the actual {@link ModelHandlerImpl}, and an {@link Interview} with 20
     * {@link TextToken}s and no {@link DetailCategory} assignment.
     */
    @Before
    public void prepareInterview() {
        this.project = new AisProject("test", ModelHandlerTest.categoryModel.provide());
        this.modelHandler = new ModelHandlerImpl(this.project);
        this.interview = this.modelHandler.createInterview("Subj123");
        this.modelHandler.setInterviewText(this.interview, "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20\n  ");
        this.paragraphStartToken = this.interview.getText().get(0);
    }

    /**
     * Test: delete interviews via the {@link AisModelHandler} interface.
     */
    @Test
    public void testDeleteInterview() {
        Assert.assertEquals(2, this.modelHandler.createInterview("Subj123").getIndex());
        final Interview deleteTargetOne = this.modelHandler.createInterview("Subj123");
        Assert.assertEquals(3, deleteTargetOne.getIndex());
        final Interview followerOfDeleteTargetOne = this.modelHandler.createInterview("Subj123");
        Assert.assertEquals(4, followerOfDeleteTargetOne.getIndex());
        Assert.assertEquals(1, this.modelHandler.createInterview("Subj124").getIndex());
        Assert.assertEquals(2, this.modelHandler.createInterview("Subj124").getIndex());
        final Interview deleteTargetTwo = this.modelHandler.createInterview("Subj125");
        Assert.assertEquals(1, deleteTargetTwo.getIndex());
        Assert.assertTrue(CollectionUtil.containsInstance(this.project.getInterviews(), deleteTargetOne));
        Assert.assertTrue(CollectionUtil.containsInstance(this.project.getInterviews(), deleteTargetTwo));
        final int interviewCount = this.project.getInterviews().size();
        this.modelHandler.deleteInterview(deleteTargetOne);
        Assert.assertEquals("Number of interviews after first delete", interviewCount - 1, this.project.getInterviews().size());
        Assert.assertEquals("Index of fourth interview, after prior interview was deleted", 3, followerOfDeleteTargetOne.getIndex());
        Assert.assertFalse(CollectionUtil.containsInstance(this.project.getInterviews(), deleteTargetOne));
        Assert.assertTrue(CollectionUtil.containsInstance(this.project.getInterviews(), deleteTargetTwo));
        this.modelHandler.deleteInterview(deleteTargetTwo);
        Assert.assertEquals("Number of interviews after second delete", interviewCount - 2, this.project.getInterviews().size());
        Assert.assertFalse(CollectionUtil.containsInstance(this.project.getInterviews(), deleteTargetTwo));
    }

    /**
     * Test: replace the project's category model with another one, replacing already assigned categories 1-to-0 (effectively removing the
     * assignment), 1-to-1, and 2-to-1 (combining assignments of two old categories to a single new one).
     *
     * @throws HmxException
     *             error when setting up the interview with assigned detail categories
     */
    @Test
    public void testReplaceCategoryModel() throws HmxException {
        final List<DetailCategory> oldSelectableCategories = ModelHandlerTest.categoryModel.provideSelectables();
        final List<TextToken> text = this.getFlatTokenList(this.paragraphStartToken);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(1, 4), oldSelectableCategories.get(0));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(5, 6), oldSelectableCategories.get(1));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(8, 10), oldSelectableCategories.get(2));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(10, 11), oldSelectableCategories.get(3));
        this.assertTokenState(text.get(0), true, null, true);
        this.assertTokenState(text.get(1), true, oldSelectableCategories.get(0), false);
        this.assertTokenState(text.get(2), false, oldSelectableCategories.get(0), false);
        this.assertTokenState(text.get(3), false, oldSelectableCategories.get(0), true);
        this.assertTokenState(text.get(4), true, null, true);
        this.assertTokenState(text.get(5), true, oldSelectableCategories.get(1), true);
        this.assertTokenState(text.get(6), true, null, false);
        this.assertTokenState(text.get(7), false, null, true);
        this.assertTokenState(text.get(8), true, oldSelectableCategories.get(2), false);
        this.assertTokenState(text.get(9), false, oldSelectableCategories.get(2), true);
        this.assertTokenState(text.get(10), true, oldSelectableCategories.get(3), true);
        this.assertTokenState(text.get(11), true, null, false);
        final MutableDetailCategoryModel newModel = new MutableDetailCategoryModel();
        final Map<DetailCategory, DetailCategory> mappedOldToNew = new HashMap<DetailCategory, DetailCategory>();
        final DetailCategory firstCategory = new DetailCategory(null, "A", "Category A", true, Color.BLACK, null);
        final DetailCategory parent = new DetailCategory(null, "B", "Parent", false, Color.BLUE, null);
        final DetailCategory secondCategory = new DetailCategory(parent, "C", "", true, null, KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, true));
        final DetailCategory thirdCategory = new DetailCategory(parent, "D", "Category D", true, Color.YELLOW, null);
        final List<DetailCategory> newCategories = Arrays.asList(firstCategory, parent, secondCategory, thirdCategory);
        newModel.addAll(newCategories);
        mappedOldToNew.put(oldSelectableCategories.get(0), firstCategory);
        mappedOldToNew.put(oldSelectableCategories.get(2), secondCategory);
        mappedOldToNew.put(oldSelectableCategories.get(3), secondCategory);
        this.modelHandler.replaceCategoryModel(newModel, mappedOldToNew);
        Assert.assertEquals(newCategories, this.project.provide());
        Assert.assertEquals(Arrays.asList(firstCategory, secondCategory, thirdCategory), this.project.provideSelectables());
        this.assertTokenState(text.get(0), true, null, true);
        this.assertTokenState(text.get(1), true, firstCategory, false);
        this.assertTokenState(text.get(2), false, firstCategory, false);
        this.assertTokenState(text.get(3), false, firstCategory, true);
        this.assertTokenState(text.get(4), true, null, false);
        this.assertTokenState(text.get(5), false, null, false);
        this.assertTokenState(text.get(6), false, null, false);
        this.assertTokenState(text.get(7), false, null, true);
        this.assertTokenState(text.get(8), true, secondCategory, false);
        this.assertTokenState(text.get(9), false, secondCategory, true);
        this.assertTokenState(text.get(10), true, secondCategory, true);
        this.assertTokenState(text.get(11), true, null, false);
    }

    /**
     * Test: change the participant id for a single interview to a participant that already exists.
     */
    @Test
    public void testSetParticipantIdForSingleInterview() {
        final Interview secondInterview = this.modelHandler.createInterview("Subj124");
        Assert.assertEquals("Subj123", this.interview.getParticipantId());
        Assert.assertEquals(1, this.interview.getIndex());
        Assert.assertEquals(1, secondInterview.getIndex());
        this.modelHandler.setParticipantId(this.interview, "Subj124");
        Assert.assertEquals("Subj124", this.interview.getParticipantId());
        Assert.assertEquals(2, this.interview.getIndex());
        Assert.assertEquals(1, secondInterview.getIndex());
    }

    /**
     * Test: change the participant id for an interview, affecting the index of following interviews with the same (original) participant id.
     */
    @Test
    public void testSetParticipantIdForInterviewInGroup() {
        final Interview secondInterview = this.modelHandler.createInterview("Subj123");
        final Interview thirdInterview = this.modelHandler.createInterview("Subj123");
        final Interview fourthInterview = this.modelHandler.createInterview("Subj123");
        Assert.assertEquals(1, this.interview.getIndex());
        Assert.assertEquals(2, secondInterview.getIndex());
        Assert.assertEquals(3, thirdInterview.getIndex());
        Assert.assertEquals(4, fourthInterview.getIndex());
        Assert.assertEquals("Subj123", secondInterview.getParticipantId());
        this.modelHandler.setParticipantId(secondInterview, "Subj124");
        Assert.assertEquals("Subj124", secondInterview.getParticipantId());
        Assert.assertEquals(1, secondInterview.getIndex());
        Assert.assertEquals(1, this.interview.getIndex());
        Assert.assertEquals(2, thirdInterview.getIndex());
        Assert.assertEquals(3, fourthInterview.getIndex());
    }

    /**
     * Test: rename a participant id to a previously non-existing value without affecting any other interviews.
     */
    @Test
    public void testRenameParticipantIdToNewOne() {
        final Interview secondInterview = this.modelHandler.createInterview("Subj124");
        final Interview thirdInterview = this.modelHandler.createInterview("Subj124");
        Assert.assertEquals(1, this.interview.getIndex());
        Assert.assertEquals(1, secondInterview.getIndex());
        Assert.assertEquals(2, thirdInterview.getIndex());
        Assert.assertEquals("Subj123", this.interview.getParticipantId());
        Assert.assertEquals("Subj124", secondInterview.getParticipantId());
        Assert.assertEquals("Subj124", thirdInterview.getParticipantId());
        this.modelHandler.renameParticipant("Subj124", "Subj125");
        Assert.assertEquals("Subj123", this.interview.getParticipantId());
        Assert.assertEquals("Subj125", secondInterview.getParticipantId());
        Assert.assertEquals("Subj125", thirdInterview.getParticipantId());
        Assert.assertEquals(1, this.interview.getIndex());
        Assert.assertEquals(1, secondInterview.getIndex());
        Assert.assertEquals(2, thirdInterview.getIndex());
    }

    /**
     * Test: rename a participant id to a previously non-existing value, setting the indices of the renamed participant's interviews accordingly.
     */
    @Test
    public void testRenameParticipantIdToExistingOne() {
        final Interview secondInterview = this.modelHandler.createInterview("Subj124");
        final Interview thirdInterview = this.modelHandler.createInterview("Subj124");
        Assert.assertEquals(1, this.interview.getIndex());
        Assert.assertEquals(1, secondInterview.getIndex());
        Assert.assertEquals(2, thirdInterview.getIndex());
        Assert.assertEquals("Subj123", this.interview.getParticipantId());
        Assert.assertEquals("Subj124", secondInterview.getParticipantId());
        Assert.assertEquals("Subj124", thirdInterview.getParticipantId());
        this.modelHandler.renameParticipant("Subj124", "Subj123");
        Assert.assertEquals("Subj123", this.interview.getParticipantId());
        Assert.assertEquals("Subj123", secondInterview.getParticipantId());
        Assert.assertEquals("Subj123", thirdInterview.getParticipantId());
        Assert.assertEquals(1, this.interview.getIndex());
        Assert.assertEquals(2, secondInterview.getIndex());
        Assert.assertEquals(3, thirdInterview.getIndex());
    }

    /**
     * Test: move single interview in its group up (by reducing its index).
     */
    @Test
    public void testSetInterviewIndexToLowerValue() {
        final Interview secondInterview = this.modelHandler.createInterview("Subj123");
        final Interview thirdInterview = this.modelHandler.createInterview("Subj123");
        final Interview fourthInterview = this.modelHandler.createInterview("Subj123");
        final Interview fifthInterview = this.modelHandler.createInterview("Subj123");
        Assert.assertEquals(1, this.interview.getIndex());
        Assert.assertEquals(2, secondInterview.getIndex());
        Assert.assertEquals(3, thirdInterview.getIndex());
        Assert.assertEquals(4, fourthInterview.getIndex());
        Assert.assertEquals(5, fifthInterview.getIndex());
        this.modelHandler.setIndex(fourthInterview, 2);
        Assert.assertEquals(1, this.interview.getIndex());
        Assert.assertEquals(2, fourthInterview.getIndex());
        Assert.assertEquals(3, secondInterview.getIndex());
        Assert.assertEquals(4, thirdInterview.getIndex());
        Assert.assertEquals(5, fifthInterview.getIndex());
    }

    /**
     * Test: move single interview in its group down (by increasing its index).
     */
    @Test
    public void testSetInterviewIndexToHigherValue() {
        final Interview secondInterview = this.modelHandler.createInterview("Subj123");
        final Interview thirdInterview = this.modelHandler.createInterview("Subj123");
        final Interview fourthInterview = this.modelHandler.createInterview("Subj123");
        final Interview fifthInterview = this.modelHandler.createInterview("Subj123");
        Assert.assertEquals(1, this.interview.getIndex());
        Assert.assertEquals(2, secondInterview.getIndex());
        Assert.assertEquals(3, thirdInterview.getIndex());
        Assert.assertEquals(4, fourthInterview.getIndex());
        Assert.assertEquals(5, fifthInterview.getIndex());
        this.modelHandler.setIndex(secondInterview, 4);
        Assert.assertEquals(1, this.interview.getIndex());
        Assert.assertEquals(2, thirdInterview.getIndex());
        Assert.assertEquals(3, fourthInterview.getIndex());
        Assert.assertEquals(4, secondInterview.getIndex());
        Assert.assertEquals(5, fifthInterview.getIndex());
    }

    /**
     * Test: assign category:<br/>
     * origin: --------------------<br/>
     * result: X-------------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignSingleStart() throws HmxException {
        final DetailCategory detail = ModelHandlerTest.categoryModel.provideSelectables().get(0);
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(this.paragraphStartToken), detail);
        this.assertTokenState(this.paragraphStartToken, true, detail, true);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, false);
        currentToken = currentToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != null);
        this.assertTokenState(currentToken, false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: --------------------<br/>
     * result: --X-----------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignSingleMid() throws HmxException {
        final DetailCategory detail = ModelHandlerTest.categoryModel.provideSelectables().get(0);
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(this.paragraphStartToken.getFollowingToken().getFollowingToken()),
                detail);
        this.assertTokenState(this.paragraphStartToken, true, null, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        this.assertTokenState(currentToken, false, null, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, detail, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, false);
        currentToken = currentToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != null);
        this.assertTokenState(currentToken, false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: --------------------<br/>
     * result: -------------------X
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignSingleEnd() throws HmxException {
        final DetailCategory detail = ModelHandlerTest.categoryModel.provideSelectables().get(0);
        TextToken paragraphEndToken = this.paragraphStartToken;
        while (paragraphEndToken.getFollowingToken() != null) {
            paragraphEndToken = paragraphEndToken.getFollowingToken();
        }
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(paragraphEndToken), detail);
        this.assertTokenState(this.paragraphStartToken, true, null, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != paragraphEndToken);
        this.assertTokenState(currentToken, false, null, true);
        this.assertTokenState(currentToken.getFollowingToken(), true, detail, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: --------------------<br/>
     * result: XX------------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignGroupStart() throws HmxException {
        final DetailCategory detail = ModelHandlerTest.categoryModel.provideSelectables().get(0);
        this.modelHandler.assignDetailCategory(this.interview,
                Arrays.asList(this.paragraphStartToken, this.paragraphStartToken.getFollowingToken()), detail);
        this.assertTokenState(this.paragraphStartToken, true, detail, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        this.assertTokenState(currentToken, false, detail, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, false);
        currentToken = currentToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != null);
        this.assertTokenState(currentToken, false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: --------------------<br/>
     * result: --XX----------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignGroupMid() throws HmxException {
        final DetailCategory detail = ModelHandlerTest.categoryModel.provideSelectables().get(0);
        final TextToken selectionStart = this.paragraphStartToken.getFollowingToken().getFollowingToken();
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(selectionStart, selectionStart.getFollowingToken()), detail);
        this.assertTokenState(this.paragraphStartToken, true, null, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        this.assertTokenState(currentToken, false, null, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, detail, false);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, false, detail, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, false);
        currentToken = currentToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != null);
        this.assertTokenState(currentToken, false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: --------------------<br/>
     * result: ------------------XX
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignGroupEnd() throws HmxException {
        final DetailCategory detail = ModelHandlerTest.categoryModel.provideSelectables().get(0);
        TextToken paragraphEndToken = this.paragraphStartToken;
        while (paragraphEndToken.getFollowingToken() != null) {
            paragraphEndToken = paragraphEndToken.getFollowingToken();
        }
        final TextToken groupStartToken = paragraphEndToken.getPreviousToken();
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(groupStartToken, paragraphEndToken), detail);
        this.assertTokenState(this.paragraphStartToken, true, null, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != groupStartToken);
        this.assertTokenState(currentToken, false, null, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, detail, false);
        this.assertTokenState(currentToken.getFollowingToken(), false, detail, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: --------------------<br/>
     * result: X-X-----------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignTwoPartStart() throws HmxException {
        final DetailCategory detail = ModelHandlerTest.categoryModel.provideSelectables().get(0);
        this.modelHandler.assignDetailCategory(this.interview,
                Arrays.asList(this.paragraphStartToken, this.paragraphStartToken.getFollowingToken().getFollowingToken()), detail);
        this.assertTokenState(this.paragraphStartToken, true, detail, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, false, detail, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, false);
        currentToken = currentToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != null);
        this.assertTokenState(currentToken, false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: --------------------<br/>
     * result: --X-X---------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignTwoPartMid_1() throws HmxException {
        final DetailCategory detail = ModelHandlerTest.categoryModel.provideSelectables().get(0);
        final TextToken selectionStart = this.paragraphStartToken.getFollowingToken().getFollowingToken();
        this.modelHandler.assignDetailCategory(this.interview,
                Arrays.asList(selectionStart, selectionStart.getFollowingToken().getFollowingToken()), detail);
        this.assertTokenState(this.paragraphStartToken, true, null, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        this.assertTokenState(currentToken, false, null, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, detail, false);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, false, detail, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, false);
        currentToken = currentToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != null);
        this.assertTokenState(currentToken, false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: -aa-----------------<br/>
     * result: -aX-X---------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignTwoPartMid_2() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        final DetailCategory detailToBeShortened = categories.get(0);
        final DetailCategory assigned = categories.get(1);
        final TextToken selectionStart = this.paragraphStartToken.getFollowingToken().getFollowingToken();
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(selectionStart.getPreviousToken(), selectionStart),
                detailToBeShortened);
        this.modelHandler.assignDetailCategory(this.interview,
                Arrays.asList(selectionStart, selectionStart.getFollowingToken().getFollowingToken()), assigned);
        this.assertTokenState(this.paragraphStartToken, true, null, true);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        this.assertTokenState(currentToken, true, detailToBeShortened, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, assigned, false);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, false, assigned, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, false);
        currentToken = currentToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != null);
        this.assertTokenState(currentToken, false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: -aaa----------------<br/>
     * assign: --X-X--------------- (FAILING)
     *
     * @throws HmxException
     *             expected error when assigning category
     */
    @Test(expected = HmxException.class)
    public void testAssignTwoPartMid_3() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        final List<TextToken> tokens = this.getFlatTokenList(this.paragraphStartToken);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(1, 4), categories.get(0));
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(tokens.get(2), tokens.get(4)), categories.get(1));
    }

    /**
     * Test: assign category:<br/>
     * origin: --aa----------------<br/>
     * result: --XaX---------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignTwoPartMid_4() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        final DetailCategory detailToBeEnclosed = categories.get(0);
        final DetailCategory assigned = categories.get(1);
        final TextToken selectionStart = this.paragraphStartToken.getFollowingToken().getFollowingToken();
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(selectionStart, selectionStart.getFollowingToken()),
                detailToBeEnclosed);
        this.modelHandler.assignDetailCategory(this.interview,
                Arrays.asList(selectionStart, selectionStart.getFollowingToken().getFollowingToken()), assigned);
        this.assertTokenState(this.paragraphStartToken, true, null, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        this.assertTokenState(currentToken, false, null, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, assigned, false);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, detailToBeEnclosed, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, false, assigned, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, false);
        currentToken = currentToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != null);
        this.assertTokenState(currentToken, false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: --aaa---------------<br/>
     * result: --XaX---------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignTwoPartMid_5() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        final DetailCategory detailToBeEnclosed = categories.get(0);
        final DetailCategory assigned = categories.get(1);
        final TextToken selectionStart = this.paragraphStartToken.getFollowingToken().getFollowingToken();
        final TextToken selectionEnd = selectionStart.getFollowingToken().getFollowingToken();
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(selectionStart, selectionStart.getFollowingToken(), selectionEnd),
                detailToBeEnclosed);
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(selectionStart, selectionEnd), assigned);
        this.assertTokenState(this.paragraphStartToken, true, null, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        this.assertTokenState(currentToken, false, null, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, assigned, false);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, detailToBeEnclosed, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, false, assigned, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, false);
        currentToken = currentToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != null);
        this.assertTokenState(currentToken, false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: ---aa---------------<br/>
     * result: --XaX---------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignTwoPartMid_6() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        final DetailCategory detailToBeEnclosed = categories.get(0);
        final DetailCategory assigned = categories.get(1);
        final TextToken selectionStart = this.paragraphStartToken.getFollowingToken().getFollowingToken();
        final TextToken selectionEnd = selectionStart.getFollowingToken().getFollowingToken();
        this.modelHandler
                .assignDetailCategory(this.interview, Arrays.asList(selectionStart.getFollowingToken(), selectionEnd), detailToBeEnclosed);
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(selectionStart, selectionEnd), assigned);
        this.assertTokenState(this.paragraphStartToken, true, null, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        this.assertTokenState(currentToken, false, null, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, assigned, false);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, detailToBeEnclosed, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, false, assigned, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, false);
        currentToken = currentToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != null);
        this.assertTokenState(currentToken, false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: ---aaa--------------<br/>
     * assign: --X-X--------------- (FAILING)
     *
     * @throws HmxException
     *             expected error when assigning category
     */
    @Test(expected = HmxException.class)
    public void testAssignTwoPartMid_7() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        final List<TextToken> tokens = this.getFlatTokenList(this.paragraphStartToken);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(3, 6), categories.get(0));
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(tokens.get(2), tokens.get(4)), categories.get(1));
    }

    /**
     * Test: assign category:<br/>
     * origin: ----aa--------------<br/>
     * result: --X-Xa--------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignTwoPartMid_8() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        final DetailCategory detailToBeShortened = categories.get(0);
        final DetailCategory assigned = categories.get(1);
        final List<TextToken> tokens = this.getFlatTokenList(this.paragraphStartToken);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(4, 6), detailToBeShortened);
        
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(tokens.get(2), tokens.get(4)), assigned);
        
        this.assertTokenState(tokens.get(0), true, null, false);
        this.assertTokenState(tokens.get(1), false, null, true);
        this.assertTokenState(tokens.get(2), true, assigned, false);
        this.assertTokenState(tokens.get(3), true, null, true);
        this.assertTokenState(tokens.get(4), false, assigned, true);
        this.assertTokenState(tokens.get(5), true, detailToBeShortened, true);
        this.assertTokenState(tokens.get(6), true, null, false);
        for (final TextToken singleToken : tokens.subList(7, 19)) {
            this.assertTokenState(singleToken, false, null, false);
        }
        this.assertTokenState(tokens.get(19), false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: --aaa---------------<br/>
     * result: -XaaX---------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignTwoPartMid_9() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        final DetailCategory detailToBeShortened = categories.get(0);
        final DetailCategory assigned = categories.get(1);
        final List<TextToken> tokens = this.getFlatTokenList(this.paragraphStartToken);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(2, 5), detailToBeShortened);
        
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(tokens.get(1), tokens.get(4)), assigned);
        
        this.assertTokenState(tokens.get(0), true, null, true);
        this.assertTokenState(tokens.get(1), true, assigned, false);
        this.assertTokenState(tokens.get(2), true, detailToBeShortened, false);
        this.assertTokenState(tokens.get(3), false, detailToBeShortened, true);
        this.assertTokenState(tokens.get(4), false, assigned, true);
        this.assertTokenState(tokens.get(5), true, null, false);
        for (final TextToken singleToken : tokens.subList(6, 19)) {
            this.assertTokenState(singleToken, false, null, false);
        }
        this.assertTokenState(tokens.get(19), false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: -aaaabb-------------<br/>
     * result: -XaaXXb-------------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignTwoPartMid_10() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        final DetailCategory detailToBeShortened = categories.get(0);
        final DetailCategory assigned = categories.get(1);
        final List<TextToken> tokens = this.getFlatTokenList(this.paragraphStartToken);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(1, 5), detailToBeShortened);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(5, 7), detailToBeShortened);
        
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(tokens.get(1), tokens.get(4), tokens.get(5)), assigned);
        
        this.assertTokenState(tokens.get(0), true, null, true);
        this.assertTokenState(tokens.get(1), true, assigned, false);
        this.assertTokenState(tokens.get(2), true, detailToBeShortened, false);
        this.assertTokenState(tokens.get(3), false, detailToBeShortened, true);
        this.assertTokenState(tokens.get(4), false, assigned, false);
        this.assertTokenState(tokens.get(5), false, assigned, true);
        this.assertTokenState(tokens.get(6), true, detailToBeShortened, true);
        this.assertTokenState(tokens.get(7), true, null, false);
        for (final TextToken singleToken : tokens.subList(8, 19)) {
            this.assertTokenState(singleToken, false, null, false);
        }
        this.assertTokenState(tokens.get(19), false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: -aaaaa--------------<br/>
     * assign: --X-X--------------- (FAILING)
     *
     * @throws HmxException
     *             expected error when assigning category
     */
    @Test(expected = HmxException.class)
    public void testAssignTwoPartMid_11() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        final List<TextToken> tokens = this.getFlatTokenList(this.paragraphStartToken);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(1, 6), categories.get(0));
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(tokens.get(2), tokens.get(4)), categories.get(1));
    }

    /**
     * Test: assign category:<br/>
     * origin: --------------------<br/>
     * result: -----------------X-X
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignTwoPartEnd() throws HmxException {
        final DetailCategory detail = ModelHandlerTest.categoryModel.provideSelectables().get(0);
        TextToken paragraphEndToken = this.paragraphStartToken;
        while (paragraphEndToken.getFollowingToken() != null) {
            paragraphEndToken = paragraphEndToken.getFollowingToken();
        }
        final TextToken selectionStartToken = paragraphEndToken.getPreviousToken().getPreviousToken();
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(selectionStartToken, paragraphEndToken), detail);
        this.assertTokenState(this.paragraphStartToken, true, null, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != selectionStartToken);
        this.assertTokenState(currentToken, false, null, true);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, detail, false);
        currentToken = currentToken.getFollowingToken();
        this.assertTokenState(currentToken, true, null, true);
        this.assertTokenState(currentToken.getFollowingToken(), false, detail, true);
    }

    /**
     * Test: remove assigned categories via interrupted selection:<br/>
     * origin: --a-a---------------<br/>
     * result: --------------------
     *
     * @throws HmxException
     *             internal error when assigning/unassigning category
     */
    @Test
    public void testUnassignTwoPart() throws HmxException {
        final TextToken thirdToken = this.paragraphStartToken.getFollowingToken().getFollowingToken();
        final List<TextToken> selection = Arrays.asList(thirdToken, thirdToken.getFollowingToken().getFollowingToken());
        this.modelHandler.assignDetailCategory(this.interview, selection, ModelHandlerTest.categoryModel.provideSelectables().get(0));
        this.modelHandler.assignDetailCategory(this.interview, selection, null);
        this.assertTokenState(this.paragraphStartToken, true, null, false);
        TextToken currentToken = this.paragraphStartToken.getFollowingToken();
        do {
            this.assertTokenState(currentToken, false, null, false);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken.getFollowingToken() != null);
        this.assertTokenState(currentToken, false, null, true);
    }

    /**
     * Test: remove assigned categories via interrupted selection:<br/>
     * origin: -aabcc--------------<br/>
     * result: -a-b-c--------------
     *
     * @throws HmxException
     *             internal error when assigning/unassigning category
     */
    @Test
    public void testUnassignInterrupted() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        // prepare start setup
        final List<TextToken> tokens = this.getFlatTokenList(this.paragraphStartToken);
        final DetailCategory detailA = categories.get(0);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(1, 3), detailA);
        final DetailCategory detailB = categories.get(1);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(3, 4), detailB);
        final DetailCategory detailC = categories.get(2);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(4, 6), detailC);
        
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(tokens.get(2), tokens.get(4)), null);
        this.assertTokenState(tokens.get(0), true, null, true);
        this.assertTokenState(tokens.get(1), true, detailA, true);
        this.assertTokenState(tokens.get(2), true, null, true);
        this.assertTokenState(tokens.get(3), true, detailB, true);
        this.assertTokenState(tokens.get(4), true, null, true);
        this.assertTokenState(tokens.get(5), true, detailC, true);
        this.assertTokenState(tokens.get(6), true, null, false);
        for (final TextToken singleToken : tokens.subList(7, 19)) {
            this.assertTokenState(singleToken, false, null, false);
        }
        this.assertTokenState(tokens.get(19), false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: -aaabcddcb----------<br/>
     * result: -aaXXXXXXb----------
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignIntersecting() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        // prepare start setup
        final List<TextToken> tokens = this.getFlatTokenList(this.paragraphStartToken);
        final DetailCategory detailA = categories.get(0);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(1, 4), detailA);
        final DetailCategory detailB = categories.get(1);
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(tokens.get(4), tokens.get(9)), detailB);
        final DetailCategory detailC = detailA;
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(tokens.get(5), tokens.get(8)), detailC);
        final DetailCategory detailD = detailB;
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(6, 8), detailD);
        // confirm start setup
        this.assertTokenState(tokens.get(0), true, null, true);
        this.assertTokenState(tokens.get(1), true, detailA, false);
        this.assertTokenState(tokens.get(2), false, detailA, false);
        this.assertTokenState(tokens.get(3), false, detailA, true);
        this.assertTokenState(tokens.get(4), true, detailB, false);
        this.assertTokenState(tokens.get(5), true, detailC, false);
        this.assertTokenState(tokens.get(6), true, detailD, false);
        this.assertTokenState(tokens.get(7), false, detailD, true);
        this.assertTokenState(tokens.get(8), false, detailC, true);
        this.assertTokenState(tokens.get(9), false, detailB, true);
        this.assertTokenState(tokens.get(10), true, null, false);
        for (final TextToken singleToken : tokens.subList(11, 19)) {
            this.assertTokenState(singleToken, false, null, false);
        }
        this.assertTokenState(tokens.get(19), false, null, true);
        // assign intersecting detail category
        final DetailCategory assigned = categories.get(4);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(3, 9), assigned);
        // confirm outcome
        this.assertTokenState(tokens.get(0), true, null, true);
        this.assertTokenState(tokens.get(1), true, detailA, false);
        this.assertTokenState(tokens.get(2), false, detailA, true);
        this.assertTokenState(tokens.get(3), true, assigned, false);
        for (final TextToken singleToken : tokens.subList(4, 8)) {
            this.assertTokenState(singleToken, false, assigned, false);
        }
        this.assertTokenState(tokens.get(8), false, assigned, true);
        this.assertTokenState(tokens.get(9), true, detailB, true);
        this.assertTokenState(tokens.get(10), true, null, false);
        for (final TextToken singleToken : tokens.subList(11, 19)) {
            this.assertTokenState(singleToken, false, null, false);
        }
        this.assertTokenState(tokens.get(19), false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: aaabb-cdddeefggg----<br/>
     * result: aXXXbXcdXXXefXgg----
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignFourPart() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        // prepare start setup
        final List<TextToken> tokens = this.getFlatTokenList(this.paragraphStartToken);
        final DetailCategory detailA = categories.get(0);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(0, 3), detailA);
        final DetailCategory detailB = categories.get(1);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(3, 5), detailB);
        final DetailCategory detailC = categories.get(2);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(6, 7), detailC);
        final DetailCategory detailD = categories.get(3);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(7, 10), detailD);
        final DetailCategory detailE = categories.get(4);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(10, 12), detailE);
        final DetailCategory detailF = categories.get(5);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(12, 13), detailF);
        final DetailCategory detailG = categories.get(6);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(13, 16), detailG);
        // confirm start setup
        this.assertTokenState(tokens.get(0), true, detailA, false);
        this.assertTokenState(tokens.get(1), false, detailA, false);
        this.assertTokenState(tokens.get(2), false, detailA, true);
        this.assertTokenState(tokens.get(3), true, detailB, false);
        this.assertTokenState(tokens.get(4), false, detailB, true);
        this.assertTokenState(tokens.get(5), true, null, true);
        this.assertTokenState(tokens.get(6), true, detailC, true);
        this.assertTokenState(tokens.get(7), true, detailD, false);
        this.assertTokenState(tokens.get(8), false, detailD, false);
        this.assertTokenState(tokens.get(9), false, detailD, true);
        this.assertTokenState(tokens.get(10), true, detailE, false);
        this.assertTokenState(tokens.get(11), false, detailE, true);
        this.assertTokenState(tokens.get(12), true, detailF, true);
        this.assertTokenState(tokens.get(13), true, detailG, false);
        this.assertTokenState(tokens.get(14), false, detailG, false);
        this.assertTokenState(tokens.get(15), false, detailG, true);
        this.assertTokenState(tokens.get(16), true, null, false);
        this.assertTokenState(tokens.get(17), false, null, false);
        this.assertTokenState(tokens.get(18), false, null, false);
        this.assertTokenState(tokens.get(19), false, null, true);
        // assign four-part detail category
        final DetailCategory assigned = categories.get(7);
        this.modelHandler.assignDetailCategory(
                this.interview,
                Arrays.asList(tokens.get(1), tokens.get(2), tokens.get(3), tokens.get(5), tokens.get(8), tokens.get(9), tokens.get(10),
                        tokens.get(13)), assigned);
        // confirm outcome
        this.assertTokenState(tokens.get(0), true, detailA, true);
        this.assertTokenState(tokens.get(1), true, assigned, false);
        this.assertTokenState(tokens.get(2), false, assigned, false);
        this.assertTokenState(tokens.get(3), false, assigned, false);
        this.assertTokenState(tokens.get(4), true, detailB, true);
        this.assertTokenState(tokens.get(5), false, assigned, false);
        this.assertTokenState(tokens.get(6), true, detailC, true);
        this.assertTokenState(tokens.get(7), true, detailD, true);
        this.assertTokenState(tokens.get(8), false, assigned, false);
        this.assertTokenState(tokens.get(9), false, assigned, false);
        this.assertTokenState(tokens.get(10), false, assigned, false);
        this.assertTokenState(tokens.get(11), true, detailE, true);
        this.assertTokenState(tokens.get(12), true, detailF, true);
        this.assertTokenState(tokens.get(13), false, assigned, true);
        this.assertTokenState(tokens.get(14), true, detailG, false);
        this.assertTokenState(tokens.get(15), false, detailG, true);
        this.assertTokenState(tokens.get(16), true, null, false);
        this.assertTokenState(tokens.get(17), false, null, false);
        this.assertTokenState(tokens.get(18), false, null, false);
        this.assertTokenState(tokens.get(19), false, null, true);
    }

    /**
     * Test: assign category:<br/>
     * origin: -aa--bcb-ddee---fg--<br/>
     * result: XaaXXbcbXddXeX--fgX-
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testAssignSixPart() throws HmxException {
        final List<DetailCategory> categories = ModelHandlerTest.categoryModel.provideSelectables();
        // prepare start setup
        final List<TextToken> tokens = this.getFlatTokenList(this.paragraphStartToken);
        final DetailCategory detailA = categories.get(0);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(1, 3), detailA);
        final DetailCategory detailB = categories.get(1);
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(tokens.get(5), tokens.get(7)), detailB);
        final DetailCategory detailC = categories.get(2);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(6, 7), detailC);
        final DetailCategory detailD = categories.get(3);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(9, 11), detailD);
        final DetailCategory detailE = categories.get(4);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(11, 13), detailE);
        final DetailCategory detailF = categories.get(5);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(16, 17), detailF);
        final DetailCategory detailG = categories.get(6);
        this.modelHandler.assignDetailCategory(this.interview, tokens.subList(17, 18), detailG);
        // confirm start setup
        this.assertTokenState(tokens.get(0), true, null, true);
        this.assertTokenState(tokens.get(1), true, detailA, false);
        this.assertTokenState(tokens.get(2), false, detailA, true);
        this.assertTokenState(tokens.get(3), true, null, false);
        this.assertTokenState(tokens.get(4), false, null, true);
        this.assertTokenState(tokens.get(5), true, detailB, false);
        this.assertTokenState(tokens.get(6), true, detailC, true);
        this.assertTokenState(tokens.get(7), false, detailB, true);
        this.assertTokenState(tokens.get(8), true, null, true);
        this.assertTokenState(tokens.get(9), true, detailD, false);
        this.assertTokenState(tokens.get(10), false, detailD, true);
        this.assertTokenState(tokens.get(11), true, detailE, false);
        this.assertTokenState(tokens.get(12), false, detailE, true);
        this.assertTokenState(tokens.get(13), true, null, false);
        this.assertTokenState(tokens.get(14), false, null, false);
        this.assertTokenState(tokens.get(15), false, null, true);
        this.assertTokenState(tokens.get(16), true, detailF, true);
        this.assertTokenState(tokens.get(17), true, detailG, true);
        this.assertTokenState(tokens.get(18), true, null, false);
        this.assertTokenState(tokens.get(19), false, null, true);
        // assign four-part detail category
        final DetailCategory assigned = categories.get(7);
        this.modelHandler.assignDetailCategory(this.interview,
                Arrays.asList(tokens.get(0), tokens.get(3), tokens.get(4), tokens.get(8), tokens.get(11), tokens.get(13), tokens.get(18)),
                assigned);
        // confirm outcome
        this.assertTokenState(tokens.get(0), true, assigned, false);
        this.assertTokenState(tokens.get(1), true, detailA, false);
        this.assertTokenState(tokens.get(2), false, detailA, true);
        this.assertTokenState(tokens.get(3), false, assigned, false);
        this.assertTokenState(tokens.get(4), false, assigned, false);
        this.assertTokenState(tokens.get(5), true, detailB, false);
        this.assertTokenState(tokens.get(6), true, detailC, true);
        this.assertTokenState(tokens.get(7), false, detailB, true);
        this.assertTokenState(tokens.get(8), false, assigned, false);
        this.assertTokenState(tokens.get(9), true, detailD, false);
        this.assertTokenState(tokens.get(10), false, detailD, true);
        this.assertTokenState(tokens.get(11), false, assigned, false);
        this.assertTokenState(tokens.get(12), true, detailE, true);
        this.assertTokenState(tokens.get(13), false, assigned, false);
        this.assertTokenState(tokens.get(14), true, null, false);
        this.assertTokenState(tokens.get(15), false, null, true);
        this.assertTokenState(tokens.get(16), true, detailF, true);
        this.assertTokenState(tokens.get(17), true, detailG, true);
        this.assertTokenState(tokens.get(18), false, assigned, true);
        this.assertTokenState(tokens.get(19), true, null, true);
    }

    /**
     * Check the given token's attributes regarding an associated detail category.
     *
     * @param token
     *            the token to check
     * @param firstOfDetail
     *            if the given token is the first element of a token range with the same detail category
     * @param detail
     *            the assigned detail category (can be {@code null}
     * @param lastOfDetail
     *            if the given token is the last element of a token range with the same detail category
     */
    private void assertTokenState(final TextToken token, final boolean firstOfDetail, final DetailCategory detail, final boolean lastOfDetail) {
        Assert.assertSame(firstOfDetail, token.isFirstTokenOfDetail());
        Assert.assertSame(detail, token.getDetail());
        Assert.assertSame(lastOfDetail, token.isLastTokenOfDetail());
    }

    /**
     * Create a list of all tokens in the specified token's paragraph by following each token's getFollowingToken method.
     *
     * @param startToken
     *            first token of the targeted paragraph
     * @return list of all following tokens (including given start token)
     */
    private List<TextToken> getFlatTokenList(final TextToken startToken) {
        final List<TextToken> list = new ArrayList<TextToken>(20);
        TextToken currentToken = startToken;
        do {
            list.add(currentToken);
            currentToken = currentToken.getFollowingToken();
        } while (currentToken != null);
        return list;
    }

    /**
     * Test: reset Interview state.
     *
     * @throws HmxException
     *             internal error when assigning category
     */
    @Test
    public void testReset() throws HmxException {
        final Interview resetState = this.interview.clone();
        this.modelHandler.setParticipantId(this.interview, "Subj001");
        this.modelHandler.setInterviewText(this.interview, "1 2 3");
        this.modelHandler.assignDetailCategory(this.interview, this.interview.getText(), ModelHandlerTest.categoryModel.provideSelectables()
                .get(0));
        Assert.assertNotEquals(resetState, this.interview);
        this.modelHandler.reset(this.interview, resetState);
        Assert.assertEquals(resetState, this.interview);
    }

    /**
     * Test: count occurrences of assigned detail categories from an interview.
     *
     * @throws HmxException
     *             error when setting up the interview with assigned detail categories
     */
    @Test
    public void testCountDetailOccurrences() throws HmxException {
        final List<DetailCategory> selectables = ModelHandlerTest.categoryModel.provideSelectables();
        final List<TextToken> text = this.getFlatTokenList(this.paragraphStartToken);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(1, 4), selectables.get(0));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(5, 6), selectables.get(1));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(6, 7), selectables.get(0));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(7, 8), selectables.get(2));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(8, 10), selectables.get(2));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(10, 11), selectables.get(3));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(11, 13), selectables.get(1));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(13, 14), selectables.get(0));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(15, 16), selectables.get(0));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(16, 18), selectables.get(2));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(18, 20), selectables.get(0));
        final Map<DetailCategory, AtomicLong> result = this.modelHandler.countDetailOccurrences(this.project.getInterviews()).get(this.interview);
        Assert.assertEquals(5, result.get(selectables.get(0)).get());
        Assert.assertEquals(2, result.get(selectables.get(1)).get());
        Assert.assertEquals(3, result.get(selectables.get(2)).get());
        Assert.assertEquals(1, result.get(selectables.get(3)).get());
    }

    /**
     * Test: count tokens with assigned detail category from an interview.
     *
     * @throws HmxException
     *             error when setting up the interview with assigned detail categories
     */
    @Test
    public void testCountTokensWithAssignedDetail() throws HmxException {
        final List<DetailCategory> selectables = ModelHandlerTest.categoryModel.provideSelectables();
        final List<TextToken> text = this.getFlatTokenList(this.paragraphStartToken);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(1, 4), selectables.get(0));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(5, 6), selectables.get(1));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(6, 7), selectables.get(0));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(7, 8), selectables.get(2));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(8, 10), selectables.get(2));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(10, 11), selectables.get(3));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(11, 13), selectables.get(1));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(13, 14), selectables.get(0));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(15, 16), selectables.get(0));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(16, 18), selectables.get(2));
        this.modelHandler.assignDetailCategory(this.interview, text.subList(18, 20), selectables.get(0));
        final AtomicLong result = this.modelHandler.countTokensWithAssignedDetail(this.project.getInterviews()).get(this.interview);
        Assert.assertEquals(17, result.get());
    }

    /**
     * Test: extract detail category patterns with length two to three from an interview.
     *
     * @throws HmxException
     *             error when setting up the interview with assigned detail categories
     */
    @Test
    public void testExtractDetailPattern() throws HmxException {
        final List<DetailCategory> selectables = ModelHandlerTest.categoryModel.provideSelectables();
        final DetailCategory firstDetail = selectables.get(0);
        final DetailCategory secondDetail = selectables.get(1);
        final DetailCategory thirdDetail = selectables.get(2);
        final DetailCategory fourthDetail = selectables.get(3);
        final List<TextToken> text = this.getFlatTokenList(this.paragraphStartToken);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(1, 4), firstDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(5, 6), secondDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(6, 7), firstDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(7, 8), thirdDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(8, 10), thirdDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(10, 11), fourthDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(11, 13), secondDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(13, 14), firstDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(15, 16), firstDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(16, 18), thirdDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(18, 20), firstDetail);
        final Map<List<DetailCategory>, AtomicLong> result =
                this.modelHandler.extractDetailPattern(this.project.getInterviews(), 2, 3).get(this.interview);
        Assert.assertEquals(1, result.get(Arrays.asList(firstDetail, secondDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(firstDetail, secondDetail, firstDetail)).get());
        Assert.assertEquals(2, result.get(Arrays.asList(secondDetail, firstDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(secondDetail, firstDetail, thirdDetail)).get());
        Assert.assertEquals(2, result.get(Arrays.asList(firstDetail, thirdDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(firstDetail, thirdDetail, thirdDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(thirdDetail, thirdDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(thirdDetail, thirdDetail, fourthDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(thirdDetail, fourthDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(thirdDetail, fourthDetail, secondDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(fourthDetail, secondDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(fourthDetail, secondDetail, firstDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(secondDetail, firstDetail, firstDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(firstDetail, firstDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(firstDetail, firstDetail, thirdDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(firstDetail, thirdDetail, firstDetail)).get());
        Assert.assertEquals(1, result.get(Arrays.asList(thirdDetail, firstDetail)).get());
    }

    /**
     * Test: extract detail category sequence from interview.
     *
     * @throws HmxException
     *             error when setting up the interview with assigned detail categories
     */
    @Test
    public void testExtractDetailSequence() throws HmxException {
        final List<DetailCategory> selectables = ModelHandlerTest.categoryModel.provideSelectables();
        final DetailCategory firstDetail = selectables.get(0);
        final DetailCategory secondDetail = selectables.get(1);
        final DetailCategory thirdDetail = selectables.get(2);
        final DetailCategory fourthDetail = selectables.get(3);
        final List<TextToken> text = this.getFlatTokenList(this.paragraphStartToken);
        final List<DetailCategory> expectedSequence = new ArrayList<DetailCategory>(11);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(1, 4), firstDetail);
        expectedSequence.add(firstDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(5, 6), secondDetail);
        expectedSequence.add(secondDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(6, 7), firstDetail);
        expectedSequence.add(firstDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(7, 8), thirdDetail);
        expectedSequence.add(thirdDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(8, 10), thirdDetail);
        expectedSequence.add(thirdDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(10, 11), fourthDetail);
        expectedSequence.add(fourthDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(11, 13), secondDetail);
        expectedSequence.add(secondDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(13, 14), firstDetail);
        expectedSequence.add(firstDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(15, 16), firstDetail);
        expectedSequence.add(firstDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(16, 18), thirdDetail);
        expectedSequence.add(thirdDetail);
        this.modelHandler.assignDetailCategory(this.interview, text.subList(18, 20), firstDetail);
        expectedSequence.add(firstDetail);
        final List<DetailCategory> result = this.modelHandler.extractDetailSequence(this.interview);
        Assert.assertEquals(expectedSequence, result);
    }

    /**
     * Test: clone project and successfully validate equality of the two projects.
     */
    @Test
    public void testValidateEquality_1() {
        Assert.assertNull("Cloned project was not equal", this.modelHandler.validateEquality(this.project.clone()));
    }

    /**
     * Test: clone project, remove a detail category and validate equality of the two projects, expecting an error message.
     */
    @Test
    public void testValidateEquality_2() {
        final AisProject clone = this.project.clone();
        final List<DetailCategory> categories = new ArrayList<DetailCategory>(this.project.provide());
        categories.remove(categories.size() - 1);
        this.modelHandler.replaceCategoryModel(new MutableDetailCategoryModel().reset(categories),
                Collections.<DetailCategory, DetailCategory>emptyMap());
        Assert.assertEquals("Detail Categories do not match", this.modelHandler.validateEquality(clone));
    }

    /**
     * Test: add a second interview, clone project, remove the second interview, and validate equality of the two projects, expecting an error
     * message.
     */
    @Test
    public void testValidateEquality_3() {
        final Interview secondInterview = this.modelHandler.createInterview("Subj124");
        final AisProject clone = this.project.clone();
        this.modelHandler.deleteInterview(secondInterview);
        Assert.assertEquals("Number of Interviews does not match:    1 != 2", this.modelHandler.validateEquality(clone));
    }

    /**
     * Test: clone project, change the interview's participant id, and validate equality of the two projects, expecting an error message.
     */
    @Test
    public void testValidateEquality_4() {
        final AisProject clone = this.project.clone();
        this.modelHandler.setParticipantId(this.interview, "Subj124");
        Assert.assertEquals("Interview Subj124 (1)\nParticipant/Index does not match:    Subj123 (1)", this.modelHandler.validateEquality(clone));
    }

    /**
     * Test: clone project, change the interview's text to contain two paragraphs, and validate equality of the two projects, expecting an error
     * message.
     */
    @Test
    public void testValidateEquality_5() {
        final AisProject clone = this.project.clone();
        this.modelHandler.setInterviewText(this.interview, "1\n2");
        Assert.assertEquals("Interview Subj123 (1)\nNumber of paragraphs differ    2 != 1", this.modelHandler.validateEquality(clone));
    }

    /**
     * Test: clone project, change the third token's text, and validate equality of the two projects, expecting an error message.
     */
    @Test
    public void testValidateEquality_6() {
        final AisProject clone = this.project.clone();
        this.modelHandler.setInterviewText(this.interview, "1 2 XX 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20");
        Assert.assertEquals("Interview Subj123 (1)\nA Token's text does not match:    'XX' != '3'", this.modelHandler.validateEquality(clone));
    }

    /**
     * Test: clone project, change the interview's text to contain less tokens, and validate equality of the two projects, expecting an error message.
     *
     * @throws HmxException
     *             error when setting up the interview with assigned detail categories
     */
    @Test
    public void testValidateEquality_7() throws HmxException {
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(this.paragraphStartToken.getFollowingToken().getFollowingToken()),
                this.project.provideSelectables().get(0));
        final AisProject clone = this.project.clone();
        this.modelHandler.setInterviewText(this.interview, "1 2");
        Assert.assertEquals("Interview Subj123 (1)\nA Paragraph's Token count does not match", this.modelHandler.validateEquality(clone));
    }

    /**
     * Test: clone project, changing the detail category assignment, and validate equality of the two projects, expecting an error message.<br/>
     * origin: --a-----------------<br/>
     * cloned: --------------------
     *
     * @throws HmxException
     *             error when setting up the interview with assigned detail categories
     */
    @Test
    public void testValidateEquality_8() throws HmxException {
        final AisProject clone = this.project.clone();
        final TextToken changedToken = this.paragraphStartToken.getFollowingToken().getFollowingToken();
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(changedToken), this.project.provideSelectables().get(0));
        Assert.assertEquals("Interview Subj123 (1)\nA Token's Detail Category assignment does not match: '2'     --- | !=  --- ",
                this.modelHandler.validateEquality(clone));
    }

    /**
     * Test: clone project, changing the detail category assignment, and validate equality of the two projects, expecting an error message.<br/>
     * origin: --a-----------------<br/>
     * cloned: --b-----------------
     *
     * @throws HmxException
     *             error when setting up the interview with assigned detail categories
     */
    @Test
    public void testValidateEquality_9() throws HmxException {
        final TextToken changedToken = this.paragraphStartToken.getFollowingToken().getFollowingToken();
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(changedToken), this.project.provideSelectables().get(0));
        final AisProject clone = this.project.clone();
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(changedToken), this.project.provideSelectables().get(1));
        Assert.assertEquals("Interview Subj123 (1)\nA Token's Detail Category assignment does not match: '3'    | Int2 | != | Int1 |",
                this.modelHandler.validateEquality(clone));
    }

    /**
     * Test: clone project, changing the detail category assignment, and validate equality of the two projects, expecting an error message.<br/>
     * origin: --aa----------------<br/>
     * cloned: --a-----------------
     *
     * @throws HmxException
     *             error when setting up the interview with assigned detail categories
     */
    @Test
    public void testValidateEquality_10() throws HmxException {
        final TextToken changedToken = this.paragraphStartToken.getFollowingToken().getFollowingToken();
        final DetailCategory assignedDetail = this.project.provideSelectables().get(0);
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(changedToken), assignedDetail);
        final AisProject clone = this.project.clone();
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(changedToken, changedToken.getFollowingToken()), assignedDetail);
        Assert.assertEquals("Interview Subj123 (1)\nA Token's Detail Category assignment does not match: '3'    | Int1  != | Int1 |",
                this.modelHandler.validateEquality(clone));
    }

    /**
     * Test: clone project, changing the detail category assignment, and validate equality of the two projects, expecting an error message.<br/>
     * origin: --ab----------------<br/>
     * cloned: --aa----------------
     *
     * @throws HmxException
     *             error when setting up the interview with assigned detail categories
     */
    @Test
    public void testValidateEquality_11() throws HmxException {
        final TextToken changedToken = this.paragraphStartToken.getFollowingToken().getFollowingToken().getFollowingToken();
        final DetailCategory assignedDetail = this.project.provideSelectables().get(0);
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(changedToken.getPreviousToken(), changedToken), assignedDetail);
        final AisProject clone = this.project.clone();
        this.modelHandler.assignDetailCategory(this.interview, Arrays.asList(changedToken), assignedDetail);
        Assert.assertEquals("Interview Subj123 (1)\nA Token's Detail Category assignment does not match: '3'    | Int1 | != | Int1 ",
                this.modelHandler.validateEquality(clone));
    }
}
