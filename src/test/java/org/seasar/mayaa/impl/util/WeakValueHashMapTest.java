/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WeakValueHashMapTest {

    @Test
    public void 引数なしコンストラクタの場合はデフォルトサイズの128で作成される() {
        WeakValueHashMap<String, String> testee = new WeakValueHashMap<>();

        assertEquals(128, testee.getHardSize());
    }

    @Test
    public void 指定したサイズを基準に強参照と弱参照が移動する() {
        WeakValueHashMap<String, String> testee = new WeakValueHashMap<>(2);

        testee.put("key1", "val1");
        testee.put("key2", "val2");
        assertEquals(0, testee.getDroppedCount());
        assertEquals(0, testee.getPullUpCount());
        assertEquals(0, testee.getMaxCountOfDroppedRecord());

        // key3 を追加することで key1 が弱参照になる（ドロップする）。
        // key1は参照されていないため getMaxCountOfDroppedRecord()は0のまま。
        testee.put("key3", "val3");
        assertEquals(1, testee.getDroppedCount());
        assertEquals(0, testee.getPullUpCount());
        assertEquals(0, testee.getMaxCountOfDroppedRecord());

        // key3 は現在は強参照のため参照してもDroppedカウントもPullUpカウントは上がらない。
        assertEquals("val3", testee.get("key3"));
        assertEquals(1, testee.getDroppedCount());
        assertEquals(0, testee.getPullUpCount());
        assertEquals(0, testee.getMaxCountOfDroppedRecord());

        // key4 を追加することで key2 が弱参照になる
        testee.put("key4", "val4");
        assertEquals(2, testee.getDroppedCount());
        assertEquals(0, testee.getMaxCountOfDroppedRecord());

        // key3 はまだ強参照のため参照してもDroppedカウントもPullUpカウントは上がらない。
        assertEquals(testee.get("key3"), "val3");
        assertEquals(2, testee.getDroppedCount());
        assertEquals(0, testee.getPullUpCount());
        assertEquals(0, testee.getMaxCountOfDroppedRecord());

        // key1 は弱参照から取得されるため key3 がドロップし、key1がプルアップされる。
        assertEquals("val1", testee.get("key1"));
        assertEquals(3, testee.getDroppedCount());
        assertEquals(1, testee.getPullUpCount());
        assertEquals(0, testee.getMaxCountOfDroppedRecord());

        // key1 は現在は強参照のため参照してもDroppedカウントもPullUpカウントは上がらない。
        assertEquals("val1", testee.get("key1"));
        assertEquals(3, testee.getDroppedCount());
        assertEquals(1, testee.getPullUpCount());
        assertEquals(0, testee.getMaxCountOfDroppedRecord());

        // key2 は弱参照から取得されるため key3 がドロップし、key2がプルアップされる。
        // key3は2回参照されたため getMaxCountOfDroppedRecord() が2を返却する。
        assertEquals("val2", testee.get("key2"));
        assertEquals(4, testee.getDroppedCount());
        assertEquals(2, testee.getPullUpCount());
        assertEquals(2, testee.getMaxCountOfDroppedRecord());

        assertEquals("val4", testee.get("key4"));
        assertEquals(5, testee.getDroppedCount());
        assertEquals(3, testee.getPullUpCount());
        assertEquals(2, testee.getMaxCountOfDroppedRecord());
    }

    @Test
    public void 弱参照のレコードが削除される() throws InterruptedException {
        WeakValueHashMap<String, String> testee = new WeakValueHashMap<>(2);


        testee.put("key1", new String("val1"));
        assertEquals(1, testee.size());

        testee.put("key2", new String("val2"));
        assertEquals(2, testee.size());

        for (int i = 3; i < 10; ++i) {
            testee.put("key" + i, new String("val" + i));
            assertEquals(i, testee.size());
        }

        System.gc();
        Thread.sleep(2); // GCのスレッドに処理を明け渡して完了を少し待つ

        assertEquals(2, testee.size());
    }

    @Test
    public void 指定したサイズを変更しても追従する() {
        WeakValueHashMap<String, String> testee = new WeakValueHashMap<>(2);

        assertEquals(2, testee.getHardSize());

        testee.put("key1", "val1");
        testee.put("key2", "val2");
        testee.put("key3", "val3");
        testee.put("key4", "val4");

        assertEquals(2, testee.getDroppedCount());
        assertEquals(0, testee.getPullUpCount());
        assertEquals(0, testee.getMaxCountOfDroppedRecord());

        testee.setHardSize(3);

        testee.put("key1", "val1");
        assertEquals(2, testee.getDroppedCount()); // ドロップしない
        assertEquals(1, testee.getPullUpCount());
        assertEquals(0, testee.getMaxCountOfDroppedRecord());

        testee.put("key2", "val2");
        assertEquals(3, testee.getDroppedCount()); // key3がドロップする
        assertEquals(2, testee.getPullUpCount());
        assertEquals(0, testee.getMaxCountOfDroppedRecord());

        testee.setHardSize(2);

        // 減らした時点でkey4がドロップする
        assertEquals(4, testee.getDroppedCount());

        // key1はまだ強参照のままなのでドロップしない
        testee.get("key1");
        assertEquals(4, testee.getDroppedCount());

        // key3は弱参照からプルアップされ、key2がドロップする
        testee.get("key3");
        assertEquals(5, testee.getDroppedCount());
        assertEquals(3, testee.getPullUpCount());
        assertEquals(0, testee.getMaxCountOfDroppedRecord());

    }

}

