package com.jamesellerbee.taskfireandroid.ui.task

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jamesellerbee.taskfireandroid.dal.taskfire.Task
import com.jamesellerbee.taskfireandroid.util.ServiceLocator
import com.jamesellerbee.taskfireandroid.util.to12HourFormat
import com.jamesellerbee.taskfireandroid.util.toDateString
import com.jamesellerbee.taskfireandroid.util.toDateTimeString
import com.jamesellerbee.taskfireandroid.util.toTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import kotlin.math.roundToInt

sealed class FormAction {
    data class Submit(val account: Task) : FormAction()
    data object Cancel : FormAction()
}

enum class DragAnchors {
    START, DELETE, COMPLETE, EXIT_LEFT
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Task(serviceLocator: ServiceLocator) {
    val viewModel = remember { TaskViewModel(serviceLocator) }

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    var selectedTask by remember {
        mutableStateOf<Task?>(null)
    }

    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(null) {
            viewModel.onInteraction(TaskInteraction.Refresh {
                withContext(Dispatchers.Main) {
                    pullToRefreshState.endRefresh()
                }
            })
        }
    }

    BackHandler(enabled = selectedTask != null) {
        selectedTask = null
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = false,
        sheetContent = {
            TaskForm {
                when (it) {
                    FormAction.Cancel -> {}

                    is FormAction.Submit -> {
                        viewModel.onInteraction(TaskInteraction.UpsertTask(it.account))
                    }
                }

                scope.launch {
                    scaffoldState.bottomSheetState.hide()
                }
            }
        }) {

        Scaffold(
            floatingActionButton = {
                if (selectedTask == null) {
                    FloatingActionButton(onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add task")
                    }
                }
            }
        ) { paddingValues ->
            Surface(
                Modifier
                    .padding(paddingValues)
                    .nestedScroll(pullToRefreshState.nestedScrollConnection)
            ) {
                Box {
                    Column {
                        if (selectedTask == null) {
                            Text(
                                text = "Hello, ${viewModel.username}!",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 4.dp)
                            )

                            val tasks = viewModel.tasks.collectAsState().value
                            if (tasks.isEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(text = "There are no tasks. Getting started by adding some.")
                                }
                            } else {
                                LazyColumn(
                                    Modifier
                                        .padding(top = 12.dp)
                                        .weight(1f)
                                ) {
                                    items(tasks) { task ->
                                        val taskScope = rememberCoroutineScope()
                                        val anchoredDraggableState = remember {
                                            AnchoredDraggableState(
                                                initialValue = DragAnchors.START,
                                                positionalThreshold = { distance -> distance * 0.5f },
                                                velocityThreshold = { 100f },
                                                animationSpec = tween()
                                            ).apply {
                                                updateAnchors(
                                                    DraggableAnchors {
                                                        DragAnchors.START at 0f
                                                        DragAnchors.COMPLETE at 500f
                                                        DragAnchors.DELETE at -500f
                                                        DragAnchors.EXIT_LEFT at -1000f
                                                    }
                                                )
                                            }
                                        }

                                        if (anchoredDraggableState.currentValue == DragAnchors.DELETE
                                            || anchoredDraggableState.currentValue == DragAnchors.EXIT_LEFT
                                        ) {
                                            LaunchedEffect(null) {
                                                taskScope.launch {
                                                    anchoredDraggableState.animateTo(DragAnchors.EXIT_LEFT)
                                                    viewModel.onInteraction(
                                                        TaskInteraction.DeleteTask(
                                                            task = task
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        if (anchoredDraggableState.currentValue == DragAnchors.COMPLETE) {
                                            LaunchedEffect(null) {
                                                viewModel.onInteraction(
                                                    TaskInteraction.UpsertTask(
                                                        task = task.copy(completed = !task.completed)
                                                    )
                                                )

                                                taskScope.launch {
                                                    anchoredDraggableState.animateTo(DragAnchors.START)
                                                }
                                            }
                                        }

                                        Box {
                                            if (anchoredDraggableState.offset < 0f) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "delete task",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier
                                                        .align(Alignment.CenterEnd)
                                                        .padding(end = 8.dp)
                                                )
                                            }

                                            if (anchoredDraggableState.offset > 0f) {
                                                if (task.completed) {
                                                    Icon(
                                                        imageVector = Icons.Default.RemoveDone,
                                                        contentDescription = "Mark task uncompleted",
                                                        modifier = Modifier
                                                            .align(Alignment.CenterStart)
                                                            .padding(8.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Checklist,
                                                        contentDescription = "Mark task complete",
                                                        modifier = Modifier
                                                            .align(Alignment.CenterStart)
                                                            .padding(8.dp)
                                                    )
                                                }
                                            }

                                            Row(
                                                Modifier
                                                    .padding(
                                                        start = 8.dp,
                                                        end = 8.dp,
                                                        bottom = 8.dp
                                                    )
                                                    .offset {
                                                        IntOffset(
                                                            x = anchoredDraggableState
                                                                .requireOffset()
                                                                .roundToInt(),
                                                            y = 0
                                                        )
                                                    }
                                                    .anchoredDraggable(
                                                        state = anchoredDraggableState,
                                                        orientation = Orientation.Horizontal
                                                    )
                                            ) {

                                                TaskCard(task, viewModel) {
                                                    selectedTask = it
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            TaskDetails(selectedTask = selectedTask!!)
                        }

                    }

                    PullToRefreshContainer(
                        state = pullToRefreshState, modifier = Modifier.align(
                            Alignment.TopCenter
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskCard(
    task: Task,
    viewModel: TaskViewModel,
    onTaskCardClicked: (Task) -> Unit
) {
    var showTaskActionsDropdown by remember { mutableStateOf(false) }

    Card {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .combinedClickable(
                    onClick = {
                        onTaskCardClicked(task)
                    },
                    onLongClick = {
                        showTaskActionsDropdown = true
                    }
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = {
                        viewModel.onInteraction(
                            TaskInteraction.UpsertTask(
                                task.copy(completed = !task.completed)
                            )
                        )
                    },
                )

                Text(
                    text = task.title,
                    textDecoration = if (task.completed) {
                        TextDecoration.LineThrough
                    } else {
                        null
                    },
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = task.modified.toDateString().plus(","),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = task.modified.toTimeString(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            DropdownMenu(
                expanded = showTaskActionsDropdown,
                onDismissRequest = { showTaskActionsDropdown = false }) {
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        viewModel.onInteraction(TaskInteraction.DeleteTask(task))
                        showTaskActionsDropdown = false
                    })
            }
        }
    }
}

@Composable
fun TaskDetails(selectedTask: Task) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Title",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.weight(1f))

        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit title"
            )
        }
    }
    Text(text = selectedTask.title)

    if (selectedTask.description.isNotBlank()) {
        Text(
            text = "Description",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(text = selectedTask.description)
    }

    Text(
        text = "Created on",
        style = MaterialTheme.typography.headlineSmall
    )
    Text(text = selectedTask.created.toDateTimeString())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskForm(onFormAction: (FormAction) -> Unit) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis >= System.currentTimeMillis()
        }
    })

    val timePickerState = rememberTimePickerState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
            },
            label = { Text("Title") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
            },
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = datePickerState.selectedDateMillis?.toDateString(ZoneId.of("UTC")) ?: "",
            onValueChange = {},
            label = { Text("Due date (optional)") },
            trailingIcon = {
                IconButton(onClick = {
                    datePickerState.selectedDateMillis = null
                }) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Clear date field"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusEvent {
                    if (it.isFocused) {
                        showDatePicker = true
                        keyboardController?.hide()
                        coroutineScope.launch {
                            scrollState.animateScrollTo(scrollState.value + 300)
                        }
                    }
                }
        )

        if (showDatePicker) {
            Spacer(Modifier.height(4.dp))

            DatePicker(
                state = datePickerState,
            )

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    showDatePicker = false
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Default.CheckCircle, "Dismiss date picker")
                }
            }
        }

        if (datePickerState.selectedDateMillis != null) {

            OutlinedTextField(
                value = "${String.format("%02d", timePickerState.hour)}:${
                    String.format(
                        "%02d",
                        timePickerState.minute
                    )
                }".to12HourFormat(),
                onValueChange = {},
                label = { Text("Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusEvent {
                        if (it.isFocused) {
                            keyboardController?.hide()
                            showTimePicker = true
                            coroutineScope.launch {
                                scrollState.animateScrollTo(Int.MAX_VALUE)
                            }
                        }
                    }
            )
        }

        if (showTimePicker) {
            Spacer(modifier = Modifier.height(4.dp))

            TimePicker(
                state = timePickerState,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = {
                    showTimePicker = false
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Default.CheckCircle, "")
                }
            }
        }

        if (!showDatePicker && !showTimePicker) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    onFormAction(
                        FormAction.Submit(
                            Task(
                                title = title,
                                description = description,
                                created = System.currentTimeMillis(),
                            )
                        )
                    )

                    title = ""
                    description = ""
                }) {
                    Text("Submit")
                }

                Button(onClick = {
                    title = ""
                    description = ""

                    onFormAction(FormAction.Cancel)
                }) {
                    Text("Cancel")
                }
            }
        }
    }
}