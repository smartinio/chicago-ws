<template>
  <div class="field has-addons" style="width: 100%">
    <p class="control has-icons-left has-icons-right" style="width: 100%">
      <input
        placeholder="Message"
        class="input is-rounded"
        type="text"
        v-model.trim="message"
        @keyup.enter="sendChatMessage"
      />
      <span class="icon is-small is-left">
        <i class="fas fa-comment"></i>
      </span>
    </p>
    <div class="control">
      <a @click="sendChatMessage" class="button is-info is-rounded" :disabled="!message">
        <span class="icon is-small" style="display: inline-block; margin-left: -15px">
          <i class="fas fa-paper-plane"></i>
        </span>
      </a>
    </div>
  </div>
</template>

<script>
import { SEND_ACTION } from '@/store/modules/socket/action_types'
import { SEND_CHAT_MESSAGE } from '@/dto/action/types'
import Action from '@/dto/action/Action'

export default {
  name: 'ChatSender',
  data() {
    return {
      message: ''
    }
  },
  methods: {
    sendChatMessage() {
      if (!this.message) return
      this.doAction(new Action(SEND_CHAT_MESSAGE, this.message))
      this.message = ''
    },
    doAction (actionDTO) {
      this.$store.dispatch(SEND_ACTION, actionDTO)
    },
  }
}
</script>
