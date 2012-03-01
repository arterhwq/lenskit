package org.grouplens.lenskit.knn.item;

import static org.grouplens.common.test.MoreMatchers.notANumber;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.grouplens.lenskit.GlobalItemRecommender;
import org.grouplens.lenskit.GlobalItemScorer;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Before;
import org.junit.Test;

public class TestGlobalItemItemRecommender {
    private LenskitRecommender session;
    private GlobalItemRecommender gRecommender;

    @Before
    public void setup() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Ratings.make(1, 1, 1));
        rs.add(Ratings.make(1, 5, 1));
        rs.add(Ratings.make(2, 1, 1));
        rs.add(Ratings.make(2, 7, 1));
        rs.add(Ratings.make(3, 7, 1));
        rs.add(Ratings.make(4, 1, 1));
        rs.add(Ratings.make(4, 5, 1));
        rs.add(Ratings.make(4, 7, 1));
        rs.add(Ratings.make(4, 10, 1));
        EventCollectionDAO.Factory manager = new EventCollectionDAO.Factory(rs);
        LenskitRecommenderEngineFactory factory = new LenskitRecommenderEngineFactory(manager);
        factory.setComponent(GlobalItemRecommender.class, GlobalItemItemRecommender.class);
        factory.setComponent(GlobalItemScorer.class, GlobalItemItemScorer.class);
        // this is the default
        // FIXME Let this work @mludwig
        /*factory.setComponent(UserVectorNormalizer.class, VectorNormalizer.class,
                             IdentityVectorNormalizer.class);*/
        LenskitRecommenderEngine engine = factory.create();
        session = engine.open();
        gRecommender = session.getGlobalItemRecommender();
    }
    
    /**
     * Check that we score items but do not provide rating scores.
     */
    @Test
    public void testGlobalItemScorerNoRating() {
        long[] queryItems = {1, 10};
        long[] items = { 5, 10 };
        GlobalItemItemScorer scorer = session.getComponent(GlobalItemItemScorer.class);
        assertThat(scorer, notNullValue());
        SparseVector scores = scorer.globalScore(LongArrayList.wrap(queryItems), LongArrayList.wrap(items));
        assertThat(scores, notNullValue());
        assertThat(scores.size(), equalTo(2));
        assertThat(scores.get(5), not(notANumber()));
       // assertThat(scores.get(10), equalTo(0.0));

    }
    
    /**
     * Tests <tt>globalRecommend(long)</tt>.
     */
    @Test
    public void testGlobalItemItemRecommender1() {
        LongList recs = gRecommender.globalRecommend(1);
        assertThat(recs.size(),notNullValue());
        recs = gRecommender.globalRecommend(2);
        assertTrue(recs.isEmpty());
        recs = gRecommender.globalRecommend(5);
        assertThat(recs.size(),notNullValue());
        recs = gRecommender.globalRecommend(7);
        assertThat(recs.size(),notNullValue());
        recs = gRecommender.globalRecommend(10);
        assertThat(recs.size(),notNullValue());

    }

    /**
     * Tests <tt>globalRecommend(long, int)</tt>.
     */
    @Test
    public void testGlobalItemItemRecommender2() {
        LongList recs = gRecommender.globalRecommend(1,2);
        assertEquals(recs.size(),2);
        recs = gRecommender.globalRecommend(2,1);
        assertTrue(recs.isEmpty());
        recs = gRecommender.globalRecommend(5,3);
        assertEquals(recs.size(),3);

    }
    
    /**
     * Tests <tt>globalRecommend(long, Set<Long>)</tt>.
     */
    @Test
    public void testGlobalItemItemRecommender3() {
    	HashSet<Long> candidates = new HashSet<Long>();
        LongList recs = gRecommender.globalRecommend(1,candidates);
        assertEquals(recs.size(),0);
        candidates.add(new Long(1));
    	candidates.add(new Long(5));
        recs = gRecommender.globalRecommend(1,candidates);
        assertEquals(recs.size(),1);
        assertTrue(recs.contains(5));

    }
    
    /**
     * Tests <tt>globalRecommend(long, int, Set<Long>, Set<Long>)</tt>.
     */
    @Test
    public void testGlobalItemItemRecommender4() {
    	HashSet<Long> candidates = new HashSet<Long>();
    	HashSet<Long> excludes = new HashSet<Long>();
        LongList recs = gRecommender.globalRecommend(1, 1, candidates, excludes);
        assertEquals(recs.size(),0);
        candidates.add(new Long(1));
        candidates.add(new Long(5));
        excludes.add(new Long(5));
        recs = gRecommender.globalRecommend(1, 2, candidates, excludes);
        assertEquals(recs.size(),1);
        recs = gRecommender.globalRecommend(1, -1, candidates, excludes);
        assertEquals(recs.size(),1);

    }
    
    /**
     * Tests <tt>globalRecommend(Set<Long>, int)</tt>.
     */
    @Test
    public void testGlobalItemItemRecommender5() {
    	HashSet<Long> basket = new HashSet<Long>();
    	basket.add(new Long(1));
    	basket.add(new Long(7));
        LongList recs = gRecommender.globalRecommend(basket, -1);
        assertEquals(recs.size(),2);
        recs = gRecommender.globalRecommend(basket, 1);
        assertEquals(recs.size(),1);
        assertTrue(recs.contains(5));

    }
    
    /**
     * Tests <tt>globalRecommend(Set<Long>, Set<Long>)</tt>.
     */
    @Test
    public void testGlobalItemItemRecommender6() {
    	HashSet<Long> basket = new HashSet<Long>();
    	basket.add(new Long(1));
    	HashSet<Long> candidates = new HashSet<Long>();
    	candidates.add(new Long(5));
    	candidates.add(new Long(10));
    	LongList recs = gRecommender.globalRecommend(basket, candidates);
        assertEquals(recs.size(),2);
        assertTrue(recs.contains(5));
        assertTrue(recs.contains(10));
        candidates.add(new Long(7));
        recs = gRecommender.globalRecommend(basket, candidates);
        assertEquals(recs.size(),3);

    }
    
    /**
     * Tests <tt>globalRecommend(Set<Long>, int, Set<Long>, Set<Long>)</tt>.
     */
    @Test
    public void testGlobalItemItemRecommender7() {
    	HashSet<Long> basket = new HashSet<Long>();
    	basket.add(new Long(5));
    	basket.add(new Long(10));
    	HashSet<Long> candidates = new HashSet<Long>();
    	candidates.add(new Long(1));
    	candidates.add(new Long(7));
    	HashSet<Long> excludes = new HashSet<Long>();
    	LongList recs = gRecommender.globalRecommend(basket, 1, candidates, excludes);
        assertEquals(recs.size(),1);
        excludes.add(new Long(5));
    	recs = gRecommender.globalRecommend(basket, 2, candidates, excludes);
        assertEquals(recs.size(),2);
        assertTrue(recs.contains(1));
        assertTrue(recs.contains(7));
        excludes.add(new Long(1));
    	recs = gRecommender.globalRecommend(basket, 2, candidates, excludes);
        assertEquals(recs.size(),1);
        assertTrue(recs.contains(7));

    }

}
