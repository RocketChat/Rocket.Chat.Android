package chat.rocket.core.interactors;

import static org.mockito.Mockito.*;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.core.SortDirection;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.SpotlightRoom;
import chat.rocket.core.repositories.RoomRepository;
import chat.rocket.core.repositories.SpotlightRoomRepository;
import chat.rocket.core.temp.TempSpotlightRoomCaller;

@RunWith(MockitoJUnitRunner.class)
public class AutocompleteChannelInteractorTest {

  @Mock
  RoomRepository roomRepository;

  @Mock
  SpotlightRoomRepository spotlightRoomRepository;

  @Mock
  TempSpotlightRoomCaller tempSpotlightRoomCaller;

  private AutocompleteChannelInteractor autocompleteChannelInteractor;

  @Before
  public void setUp() {
    autocompleteChannelInteractor = new AutocompleteChannelInteractor(
        roomRepository, spotlightRoomRepository, tempSpotlightRoomCaller
    );
  }

  @Test
  public void getSuggestionsForEmptyStringReturnLatestSeenOnly() throws Exception {

    List<Room> rooms = new ArrayList<>();
    rooms.add(getRoom("id1", "Name1", "c"));
    when(roomRepository.getLatestSeen(anyInt())).thenReturn(Flowable.just(rooms));

    rooms = new ArrayList<>();
    rooms.add(getRoom("id2", "Name2", "c"));
    when(roomRepository.getSortedLikeName(anyString(), any(SortDirection.class), anyInt()))
        .thenReturn(Flowable.just(rooms));

    TestSubscriber<List<SpotlightRoom>> testSubscriber = new TestSubscriber<>();

    autocompleteChannelInteractor.getSuggestionsFor("").subscribe(testSubscriber);

    List<SpotlightRoom> spotlightRooms = new ArrayList<>();
    spotlightRooms.add(getSpotlightRoom("id1", "Name1", "c"));

    testSubscriber.assertResult(spotlightRooms);
  }

  @Test
  public void getSuggestionsForNonEmptyStringReturnLatestSeenAndFromRooms() throws Exception {

    List<Room> rooms = new ArrayList<>();
    rooms.add(getRoom("id1", "Name1", "c"));
    rooms.add(getRoom("id1.1", "Ame1.1", "c"));
    when(roomRepository.getLatestSeen(anyInt())).thenReturn(Flowable.just(rooms));

    rooms = new ArrayList<>();
    rooms.add(getRoom("id1", "Name1", "c"));
    rooms.add(getRoom("id2", "Name2", "c"));
    rooms.add(getRoom("id3", "Name3", "c"));
    rooms.add(getRoom("id4", "Name4", "c"));
    rooms.add(getRoom("id5", "Name5", "c"));
    when(roomRepository.getSortedLikeName(anyString(), any(SortDirection.class), anyInt()))
        .thenReturn(Flowable.just(rooms));

    TestSubscriber<List<SpotlightRoom>> testSubscriber = new TestSubscriber<>();

    autocompleteChannelInteractor.getSuggestionsFor("N").subscribe(testSubscriber);

    List<SpotlightRoom> spotlightRooms = new ArrayList<>();
    spotlightRooms.add(getSpotlightRoom("id1", "Name1", "c"));
    spotlightRooms.add(getSpotlightRoom("id2", "Name2", "c"));
    spotlightRooms.add(getSpotlightRoom("id3", "Name3", "c"));
    spotlightRooms.add(getSpotlightRoom("id4", "Name4", "c"));
    spotlightRooms.add(getSpotlightRoom("id5", "Name5", "c"));

    testSubscriber.assertResult(spotlightRooms);
  }

  @Test
  public void getSuggestionsForMayGetFromNetwork() throws Exception {

    List<Room> rooms = new ArrayList<>();
    rooms.add(getRoom("id1", "Name1", "c"));
    rooms.add(getRoom("id1.1", "Ame1.1", "c"));
    when(roomRepository.getLatestSeen(anyInt())).thenReturn(Flowable.just(rooms));

    rooms = new ArrayList<>();
    rooms.add(getRoom("id1", "Name1", "c"));
    rooms.add(getRoom("id2", "Name2", "c"));
    when(roomRepository.getSortedLikeName(anyString(), any(SortDirection.class), anyInt()))
        .thenReturn(Flowable.just(rooms));

    List<SpotlightRoom> spotlightRooms = new ArrayList<>();
    spotlightRooms.add(getSpotlightRoom("id3", "Name3", "c"));
    when(spotlightRoomRepository.getSuggestionsFor(anyString(), any(SortDirection.class), anyInt()))
        .thenReturn(Flowable.just(spotlightRooms));

    TestSubscriber<List<SpotlightRoom>> testSubscriber = new TestSubscriber<>();

    autocompleteChannelInteractor.getSuggestionsFor("N").subscribe(testSubscriber);

    verify(tempSpotlightRoomCaller, times(1)).search(anyString());

    spotlightRooms = new ArrayList<>();
    spotlightRooms.add(getSpotlightRoom("id1", "Name1", "c"));
    spotlightRooms.add(getSpotlightRoom("id2", "Name2", "c"));
    spotlightRooms.add(getSpotlightRoom("id3", "Name3", "c"));

    testSubscriber.assertResult(spotlightRooms);
  }

  private Room getRoom(String id, String name, String type) {
    Room room = mock(Room.class);
    when(room.getId()).thenReturn(id);
    when(room.getName()).thenReturn(name);
    when(room.getType()).thenReturn(type);

    return room;
  }

  private SpotlightRoom getSpotlightRoom(String id, String name, String type) {
    return SpotlightRoom.builder()
        .setId(id).setName(name).setType(type).build();
  }
}