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
          class="button is-small is-rounded is-info is-light"
          @click="showRules = true"
          v-if="!showRules"
        >
          <span class="icon is-small"><i class="fa fa-scale-balanced"></i></span>
          <span>Show rules</span>
        </a>
        <a
          class="button is-small is-rounded is-danger is-light"
          @click="$emit('leave')"
        >
          <span class="icon is-small"><i class="fa fa-right-from-bracket"></i></span>
          <span>Leave game</span>
        </a>
      </div>

      <div class="is-flex" style="width: 400px" v-if="showRules">
        <div class="notification is-info" style="flex: 1">
          <button class="delete" aria-label="delete" @click="showRules = false"></button>
          <p><strong>{{numTrades}}</strong> trades</p>
          <p>Chicago <strong>{{chicagoBestHand ? 'requires' : 'does not require'}}</strong> best hand</p>
          <p>1 open card available at <strong>{{ oneOpenFinal ? 'the final trade' : 'every trade'}}</strong></p>
        </div>
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
  data() {
    return {
      showRules: true,
    }
  },
  computed: {
    numTrades () {
      return this.game.rules.numTrades
    },
    chicagoBestHand() {
      return this.game.rules.chicagoBestHand
    },
    oneOpenFinal() {
      return this.game.rules.oneOpen === 'FINAL'
    }
  },
  components: {
    ChatSender,
    EventLog,
    ConnectionStatus,
  }
}
</script>
