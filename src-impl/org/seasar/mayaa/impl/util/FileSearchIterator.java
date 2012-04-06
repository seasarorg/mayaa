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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class FileSearchIterator implements Iterator, Runnable {

    private File _root;
    private FilenameFilter _filenameFilter;
    private boolean _done = false;
    private File _current;
    private Thread _stepFindThread;
    private boolean _gotNext = false;

    private volatile boolean _doFindNext;
    private volatile boolean _doFindNextDone;

    public FileSearchIterator(File rootDir) {
        this(rootDir, null);
    }

    public FileSearchIterator(File rootDir, FilenameFilter filenameFilter) {
        _root = rootDir;
        _filenameFilter = filenameFilter;
    }

    public File getRoot() {
        return _root;
    }

    public FilenameFilter getFilenameFilter() {
        return _filenameFilter;
    }

    protected void startThread() {
        if (_stepFindThread == null) {
            _done = false;
            _doFindNext = false;
            _stepFindThread = new Thread(this);
            _stepFindThread.setName("fileSearch:root=" + _root + ":filter=" + _filenameFilter);
            _stepFindThread.setDaemon(true);
            _stepFindThread.start();
        }
    }

    protected void stopThread() {
        _stepFindThread = null;
    }

    public boolean hasNext() {
        if (_done && _current == null) {
            return false;
        }
        startThread();
        _doFindNext = true;
        while (_done == false && _doFindNextDone == false) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
        boolean found = _doFindNextDone;
        _doFindNext = false;
        while (_done == false && _doFindNextDone == true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
        if (_done && found == false) {
            return false;
        }
        _gotNext = found;
        return found;
    }

    public Object next() {
        if (_gotNext == false) {
            if (hasNext() == false) {
                throw new NoSuchElementException();
            }
        }
        _gotNext = false;
        return _current;
    }

    private Thread _internalCurrentThread;

    public void run() {
        try {
            _done = false;
            _current = null;
            _internalCurrentThread = Thread.currentThread();
            findFile(_root, _root);
        } catch(InterruptedException e) {
            // no operation
        }
        _done = true;
    }

    protected void findFile(File root, File dir)
            throws InterruptedException {
        File[] files;
        if (_filenameFilter == null) {
            files = dir.listFiles();
        } else {
            files = dir.listFiles(_filenameFilter);
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isFile() && file.isHidden() == false) {
                while (_doFindNext == false
                        && _internalCurrentThread == _stepFindThread) {
                    Thread.sleep(1);
                }
                if (_internalCurrentThread != _stepFindThread) {
                    return;
                }
                _current = file;
                _doFindNextDone = true;
                while (_doFindNext &&
                        _internalCurrentThread == _stepFindThread) {
                    Thread.sleep(1);
                }
                _doFindNextDone = false;
                if (_internalCurrentThread != _stepFindThread) {
                    return;
                }
            }
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                findFile(root, file);
                if (_internalCurrentThread != _stepFindThread) {
                    return;
                }
            }
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected void finalize() throws Throwable {
        stopThread();
        super.finalize();
    }

}
