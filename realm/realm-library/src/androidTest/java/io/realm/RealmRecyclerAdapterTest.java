package io.realm;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

import io.realm.entities.AllTypes;
import io.realm.entities.RealmRecyclerAdapter;
import io.realm.entities.UnsupportedCollection;
import io.realm.rule.TestRealmConfigurationFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class RealmRecyclerAdapterTest {

    @Rule
    public final UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();
    @Rule
    public final TestRealmConfigurationFactory configFactory = new TestRealmConfigurationFactory();
    private Context context;

    private final static String FIELD_STRING = "columnString";
    private final static int TEST_DATA_SIZE = 47;

    private boolean automaticUpdate = true;
    private Realm testRealm;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getContext();
        RealmConfiguration realmConfig = configFactory.createConfiguration();
        testRealm = Realm.getInstance(realmConfig);

        populateTestRealm(testRealm, TEST_DATA_SIZE);
    }

    private void populateTestRealm(Realm testRealm, int objects) {
        testRealm.beginTransaction();
        for (int i = 0; i < objects; i++) {
            AllTypes allTypes = testRealm.createObject(AllTypes.class);
            allTypes.setColumnString("test data " + i);
        }
        testRealm.commitTransaction();
    }

    @After
    public void tearDown() throws Exception {
        testRealm.close();
    }

    @Test
    public void constructor_testRecyclerAdapterParameterExceptions() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        try {
            new RealmRecyclerAdapter(null, resultList, automaticUpdate);
            fail("Should throw exception if context is null");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    @UiThreadTest
    public void clear_testRemoveAllDataFromAdapter() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, automaticUpdate);
        realmAdapter.clear();

        assertEquals(0, realmAdapter.getItemCount());
        assertEquals(0, resultList.size());
    }

    @Test
    @UiThreadTest
    public void clear_testAdapterUpdatesOnRemoveAllData() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, false);
        RecyclerView.AdapterDataObserver adapterObserver = mock(RecyclerView.AdapterDataObserver.class);
        realmAdapter.registerAdapterDataObserver(adapterObserver);
        realmAdapter.clear();

        verify(adapterObserver, times(1)).onChanged();
        verify(adapterObserver, never()).onItemRangeInserted(anyInt(), anyInt());
        verify(adapterObserver, never()).onItemRangeMoved(anyInt(), anyInt(), anyInt());
        verify(adapterObserver, never()).onItemRangeChanged(anyInt(), anyInt());
        verify(adapterObserver, never()).onItemRangeRemoved(anyInt(), anyInt());
    }

    @Test
    @UiThreadTest
    public void clear_testRemoveAllDataFromAdapterNullResult() {
        try {
            RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, automaticUpdate);
            realmAdapter.clear();
        } catch (IllegalStateException e) {
            fail("Adapter should handle data inconsistency");
        }
    }

    @Test
    @UiThreadTest
    public void removeItem_testRemoveFromAdapterNullResult() {
        try {
            RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, automaticUpdate);
            realmAdapter.removeItem(0);
        } catch (IllegalStateException e) {
            fail("Adapter should handle data inconsistency");
        }
    }

    @Test
    @UiThreadTest
    public void removeItem_testRemoveFromAdapter() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, automaticUpdate);
        realmAdapter.removeItem(0);
        assertEquals(TEST_DATA_SIZE - 1, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void removeItem_testRemoveFromAdapterNoAutoUpdates() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, false);
        RecyclerView.AdapterDataObserver adapterObserver = mock(RecyclerView.AdapterDataObserver.class);
        realmAdapter.registerAdapterDataObserver(adapterObserver);
        realmAdapter.removeItem(0);

        verify(adapterObserver, never()).onChanged();
        verify(adapterObserver, never()).onItemRangeInserted(anyInt(), anyInt());
        verify(adapterObserver, never()).onItemRangeMoved(anyInt(), anyInt(), anyInt());
        verify(adapterObserver, never()).onItemRangeChanged(anyInt(), anyInt());
        verify(adapterObserver, times(1)).onItemRangeRemoved(0, 1);
    }

    @Test
    @UiThreadTest
    public void updateData_realmResultInAdapter() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        resultList.sort(FIELD_STRING);
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, false);
        assertEquals(resultList.first().getColumnString(), realmAdapter.getData().first().getColumnString());
        assertEquals(resultList.size(), realmAdapter.getData().size());

        testRealm.beginTransaction();
        AllTypes allTypes = testRealm.createObject(AllTypes.class);
        allTypes.setColumnString("test data " + TEST_DATA_SIZE);
        testRealm.commitTransaction();
        assertEquals(resultList.last().getColumnString(), realmAdapter.getData().last().getColumnString());
        assertEquals(resultList.size(), realmAdapter.getData().size());

        RealmResults<AllTypes> emptyResultList = testRealm.where(AllTypes.class).equalTo(FIELD_STRING, "Not there").findAll();
        realmAdapter.updateData(emptyResultList);
        assertEquals(emptyResultList.size(), realmAdapter.getData().size());
    }

    @Test
    @UiThreadTest
    public void updateData_realmUnsupportedCollectionInAdapter() {
        try {
            RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, automaticUpdate);
            realmAdapter.updateData(new UnsupportedCollection<AllTypes>());
            fail("Should throw exception if there is unsupported collection");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    @UiThreadTest
    public void testSortWithAdapter() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        resultList.sort(FIELD_STRING, Sort.DESCENDING);
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, automaticUpdate);
        assertEquals(resultList.first().getColumnString(), realmAdapter.getData().first().getColumnString());
        assertEquals(resultList.size(), realmAdapter.getData().size());

        resultList.sort(FIELD_STRING);

        assertEquals(resultList.last().getColumnString(), realmAdapter.getData().last().getColumnString());
        assertEquals(resultList.get(TEST_DATA_SIZE / 2).getColumnString(), realmAdapter.getData().get(TEST_DATA_SIZE / 2).getColumnString());
        assertEquals(resultList.size(), realmAdapter.getData().size());
    }

    @Test
    @UiThreadTest
    public void testEmptyRealmResult() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).equalTo(FIELD_STRING, "Not there").findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, automaticUpdate);
        assertEquals(0, realmAdapter.getData().size());
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItem_testGettingData() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, automaticUpdate);

        assertEquals(resultList.get(0).getColumnString(), realmAdapter.getItem(0).getColumnString());
        assertEquals(resultList.size(), realmAdapter.getData().size());
        assertEquals(resultList.last().getColumnString(), realmAdapter.getData().last().getColumnString());
    }

    @Test
    @UiThreadTest
    public void getItem_testGettingNullData() {
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, automaticUpdate);
        assertNull(realmAdapter.getItem(0));
    }

    @Test
    @UiThreadTest
    public void getItemId_testGetItemId() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, automaticUpdate);
        for (int i = 0; i < resultList.size(); i++) {
            assertEquals(i, realmAdapter.getItemId(i));
        }
    }

    @Test
    @UiThreadTest
    public void getItemCount_testGetCount() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, automaticUpdate);
        assertEquals(TEST_DATA_SIZE, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNullResults() {
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, automaticUpdate);
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNotValidResults() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, automaticUpdate);

        testRealm.close();
        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNonNullToNullResults() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, automaticUpdate);
        realmAdapter.updateData(null);

        assertEquals(0, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void getItemCount_testNullToNonNullResults() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, null, automaticUpdate);
        realmAdapter.updateData(resultList);

        assertEquals(TEST_DATA_SIZE, realmAdapter.getItemCount());
    }

    @Test
    @UiThreadTest
    public void testGetViewHolder() {
        RealmResults<AllTypes> resultList = testRealm.where(AllTypes.class).findAll();
        RealmRecyclerAdapter realmAdapter = new RealmRecyclerAdapter(context, resultList, automaticUpdate);

        RealmRecyclerAdapter.ViewHolder holder = realmAdapter.onCreateViewHolder(new FrameLayout(context), 0);
        assertNotNull(holder.textView);

        realmAdapter.onBindViewHolder(holder, 0);
        assertEquals(resultList.get(0).getColumnString(), holder.textView.getText());
    }
}
