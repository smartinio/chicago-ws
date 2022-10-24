<template>
  <div
    class="is-flex is-flex-direction-column is-justify-content-space-between"
    style="background-color: #f5f5f5; border-radius: 20px; padding: 15px"
  >
    <div class="is-flex is-flex-direction-column" style="flex: 1; overflow: hidden">
      <ConnectionStatus
        :connected="connected"
        @reconnect="$emit('reconnect')"
      />
      <div style="height: 15px" />
      <EventLog :game="game" />
    </div>
    <div style="padding-top: 15px">
      <ChatSender />
    </div>
  </div>
</template>

<script>
import ConnectionStatus from './ConnectionStatus'
import ChatSender from './ChatSender'
import EventLog from './EventLog'

export default {
  name: 'Chat',
  props: ['game', 'connected'],
  components: {
    ChatSender,
    EventLog,
    ConnectionStatus,
  }
}
</script>
