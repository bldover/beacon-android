package com.bldover.beacon.ui.screens.editor.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bldover.beacon.data.model.Artist
import com.bldover.beacon.data.model.Event
import com.bldover.beacon.data.model.Venue
import com.bldover.beacon.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

sealed class EventEditorState {
    data object Loading : EventEditorState()
    data class Success(
        val uuid: String,
        val savedEvent: Event,
        val tempEvent: Event
    ) : EventEditorState()
    data class Error(val message: String) : EventEditorState()
}

@HiltViewModel
class EventEditorViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventEditorState>(EventEditorState.Loading)
    val uiState: StateFlow<EventEditorState> = _uiState.asStateFlow()

    fun loadEvent(eventId: String?, uuid: String) {
        Timber.i("Loading edit event ID $eventId")
        if (_uiState.value is EventEditorState.Success) {
            val state = _uiState.value as EventEditorState.Success
            if (state.uuid == uuid) {
                Timber.d("Loading edit event - skipping due to already being loaded")
                return
            }
        }
        viewModelScope.launch {
            _uiState.value = EventEditorState.Loading
            try {
                val event = if (eventId != null) {
                    eventRepository.getEvent(eventId)
                } else {
                    Event(
                        artists = emptyList(),
                        date = LocalDate.now(),
                        venue = Venue(name = "", city = "", state = ""),
                        purchased = false
                    )
                }
                _uiState.value = EventEditorState.Success(uuid, event, event.copy())
            } catch (e: Exception) {
                Timber.e(e,"Failed to load event $eventId")
                _uiState.value = EventEditorState.Error("Failed to load event")
            }
        }
    }

    fun updateHeadliner(headliner: Artist?) {
        Timber.i("Updating headliner $headliner")
        if (_uiState.value !is EventEditorState.Success) {
            Timber.d("Updating headliner - not in success state")
            return
        }
        val state = (_uiState.value as EventEditorState.Success)
        Timber.d("Updating headliner - previous artists ${state.tempEvent.artists}")
        val artists = state.tempEvent.artists.toMutableList().apply {
            removeAll { it.headliner }
            headliner?.let {
                it.headliner = true
                add(it)
            }
        }
        Timber.d("Updating headliner - new artists $artists")
        _uiState.value = EventEditorState.Success(
            uuid = state.uuid,
            savedEvent = state.savedEvent,
            tempEvent = state.tempEvent.copy(artists = artists)
        )
        Timber.i("Updated headliner - success")
    }

    fun addOpener(opener: Artist) {
        Timber.i("Adding opener $opener")
        if (_uiState.value !is EventEditorState.Success) {
            Timber.d("Updating openers - not in success state")
            return
        }
        val state = (_uiState.value as EventEditorState.Success)
        Timber.d("Adding opener - previous artists ${state.tempEvent.artists}")
        val artists = state.tempEvent.artists.toMutableList().apply {
            add(opener)
        }
        Timber.d("Adding opener - new artists $artists")
        _uiState.value = EventEditorState.Success(
            uuid = state.uuid,
            savedEvent = state.savedEvent,
            tempEvent = state.tempEvent.copy(artists = artists)
        )
        Timber.i("Adding opener - success")
    }

    fun removeOpener(opener: Artist) {
        Timber.i("Removing opener $opener")
        if (_uiState.value !is EventEditorState.Success) {
            Timber.d("Updating openers - not in success state")
            return
        }
        val state = (_uiState.value as EventEditorState.Success)
        Timber.d("Removing opener - previous artists ${state.tempEvent.artists}")
        val artists = state.tempEvent.artists.toMutableList().apply {
            remove(opener)
        }
        Timber.d("Removing opener - new artists $artists")
        _uiState.value = EventEditorState.Success(
            uuid = state.uuid,
            savedEvent = state.savedEvent,
            tempEvent = state.tempEvent.copy(artists = artists)
        )
        Timber.i("Removing opener - success")
    }

    fun updateVenue(venue: Venue) {
        Timber.i("Updating venue $venue")
        if (_uiState.value !is EventEditorState.Success) {
            Timber.d("Updating venue - not in success state")
            return
        }
        val state = (_uiState.value as EventEditorState.Success)
        Timber.d("Updating venue - previous venue ${state.tempEvent.venue}")
        _uiState.value = EventEditorState.Success(
            uuid = state.uuid,
            savedEvent = state.savedEvent,
            tempEvent = state.tempEvent.copy(venue = venue)
        )
        Timber.i("Updated venue - success")
    }

    fun updateDate(date: LocalDate) {
        Timber.i("Updating date $date")
        if (_uiState.value !is EventEditorState.Success) {
            Timber.d("Updating date - not in success state")
            return
        }
        val state = (_uiState.value as EventEditorState.Success)
        Timber.d("Updating date - previous date ${state.tempEvent.date}")
        _uiState.value = EventEditorState.Success(
            uuid = state.uuid,
            savedEvent = state.savedEvent,
            tempEvent = state.tempEvent.copy(date = date)
        )
        Timber.i("Updated date - success")
    }

    fun updatePurchased(purchased: Boolean) {
        Timber.i("Updating purchased $purchased")
        if (_uiState.value !is EventEditorState.Success) {
            Timber.d("Updating purchased - not in success state")
            return
        }
        val state = (_uiState.value as EventEditorState.Success)
        Timber.d("Updating purchased - previous purchased ${state.tempEvent.purchased}")
        _uiState.value = EventEditorState.Success(
            uuid = state.uuid,
            savedEvent = state.savedEvent,
            tempEvent = state.tempEvent.copy(purchased = purchased)
        )
        Timber.i("Updated purchased - success")
    }
}