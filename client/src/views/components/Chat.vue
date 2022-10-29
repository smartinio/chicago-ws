<template>
  <div
    class="is-flex is-flex-direction-column is-justify-content-space-between"
    style="background-color: #f5f5f5; border-radius: 20px; padding: 15px"
  >
    <div class="is-flex is-flex-direction-column" style="flex: 1; overflow: hidden">
      <div class="is-flex is-justify-content-space-between">
        <ConnectionStatus
          :connected="connected"
        />
        <a
          class="button is-small is-rounded is-danger is-light"
          @click="$emit('leave')"
        >
          <span class="icon is-small"><i class="fa fa-right-from-bracket"></i></span>
          <span>Leave game</span>
        </a>
      </div>
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
