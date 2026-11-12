# Dialogs

Alerts for an uncommon action that cannot be undone; action sheets for choices after an intentional action (#16).

An `.alert` sits in `.center` over a `.scrim`: a title, one line saying what happens, two buttons with the destructive one named ("Discard"). An `.asheet` lists the choices as centred rows with a grey `.row.head` that counts what is at stake, `.row.destructive` in red, and `.row.cancel` in its own block. No alert after a destructive choice in an action sheet. Prompts are alerts with a `.field`.
