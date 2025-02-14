package androidx.core.telecom.test;

import androidx.core.telecom.extensions.Capability;
import androidx.core.telecom.extensions.Participant;
import androidx.core.telecom.test.ITestAppControlCallback;

// NOTE: only supports one voip call at a time right now + suspend functions are not supported by
// AIDL :(
@JavaPassthrough(annotation="@androidx.annotation.RestrictTo(androidx.annotation.RestrictTo.Scope.LIBRARY)")
interface ITestAppControl {
  void setCallback(in ITestAppControlCallback callback);
  String addCall(in List<Capability> capabilities, boolean isOutgoing);
  void updateParticipants(in List<Participant> participants);
  void updateActiveParticipant(in Participant participant);
  void updateRaisedHands(in List<Participant> raisedHandsParticipants);
}