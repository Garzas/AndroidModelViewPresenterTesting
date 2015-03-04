/*
 * Copyright (C) 2014 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.cachedmodel;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.anim.FadeAnimationHelper;
import com.example.cachedmodel.adapters.CommentsAdapter;
import com.example.model.Task;
import com.example.presenter.TasksPresenter;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

public class UglyMainActivity extends Activity {

    private CommentsAdapter mAdapter;

    private TasksPresenter tasksPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FadeAnimationHelper fadeAnimationHelper = new FadeAnimationHelper(getResources());

        tasksPresenter = restoreRetainedFragment();

        final View errorView = findViewById(R.id.main_error);
        final ListView listView = (ListView) findViewById(R.id.main_list);
        final View progressView = findViewById(R.id.main_progress);
        final TextView commentTextView = (TextView) findViewById(R.id.main_comment_edit_text);
        final View sendButton = findViewById(R.id.main_send_button);
        final View commentProgress = findViewById(R.id.main_comment_progress);

        mAdapter = new CommentsAdapter(this);
        listView.setAdapter(mAdapter);

        tasksPresenter.register(new TasksPresenter.Listener() {

            @Override
            public void swapData(@Nonnull final ImmutableList<Task> tasks) {
                mAdapter.swapData(tasks);
            }

            @Override
            public void showProgress(final boolean showProgress) {
                fadeAnimationHelper.setVisibility(progressView, showProgress ? View.VISIBLE : View.GONE);
            }

            @Override
            public String getTaskName() {
                return String.valueOf(commentTextView.getText());
            }

            @Override
            public void showTaskProgress(final boolean showProgress) {
                fadeAnimationHelper.setVisibility(commentProgress, showProgress ? View.VISIBLE : View.GONE);
            }

            @Override
            public void showError(final boolean showError) {
                fadeAnimationHelper.setVisibility(errorView, showError ? View.VISIBLE : View.GONE);
            }

            @Override
            public void clearTaskTextView() {
                commentTextView.setText("");
            }

        });

        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tasksPresenter.errorClick();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                tasksPresenter.sendClick();
            }
        });
    }

    @Override
    protected void onDestroy() {
        tasksPresenter.unregister();
        super.onDestroy();
    }

    private TasksPresenter restoreRetainedFragment() {
        final FragmentManager fm = getFragmentManager();
        final RetainedFragment retainedFragment = (RetainedFragment) fm.findFragmentByTag("data");
        if (retainedFragment != null) {
            return retainedFragment.getData();
        }

        final RetainedFragment newRetainedFragment = new RetainedFragment();
        fm.beginTransaction().add(newRetainedFragment, "data").commit();
        final TasksPresenter presenter = MainApplication
                .fromApplication(getApplication())
                .objectGraph()
                .plus(new Module())
                .get(TasksPresenter.class);
        newRetainedFragment.setData(presenter);
        return presenter;
    }

    public static class RetainedFragment extends Fragment {

        private TasksPresenter data;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        public void setData(TasksPresenter data) {
            this.data = data;
        }

        public TasksPresenter getData() {
            return data;
        }
    }

    @dagger.Module(
            injects = {
                    TasksPresenter.class
            },
            addsTo = MainApplication.Module.class
    )
    class Module {

    }

}