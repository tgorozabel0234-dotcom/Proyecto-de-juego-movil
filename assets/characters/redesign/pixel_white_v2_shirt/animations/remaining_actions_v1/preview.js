'use strict';
const catalog=window.ANIMATION_CATALOG;
const $=id=>document.getElementById(id);
const groupNames={combat:'COMBATE',grapple:'GANCHO',reaction:'REACCIONES'};
const descriptions={
 attack_side:'Preparación, barrido de hoja extendida y seguimiento. El arma nace del antebrazo metálico; el lienzo deja margen a la punta.',
 attack_up:'Preparación baja, corte ascendente y seguimiento alto. La pose activa usa un original independiente para conservar la punta entera.',
 attack_down:'Sin impacto: preparación y barrido hacia abajo. Con impacto: añade la pose de rebote. La subida real se aplicará en la física del juego.',
 thrust:'Se inicia corriendo desde el suelo. Sin impacto recupera la carrera; con impacto muestra la salida de subida. No representa un ascenso automático al atacar.',
 aura:'Activación y aura tenue. Se conserva la segunda pose para revisar el efecto; no añade curación ni cambia daño en esta muestra.',
 hook_strike:'Ataque rápido desde el péndulo. La salida de subida corresponde al impacto; la cuerda se liberará al integrar ese evento.',
 whirlwind:'Tres posiciones de barrido con el cuerpo suspendido. El cuerpo se orienta con la cuerda en la prueba inferior; este bucle no aplica daño ni suelta el gancho por sí solo.',
 hook_fire:'Preparación y salida del gancho desde el antebrazo metálico. La cuerda se dibujará por separado; la separación de brazo y torso para apuntar desde el suelo se documenta para la integración.',
 pendulum:'Una pose de agarre sostenido. No se necesita un bucle de giro: prueba su orientación real con los controles de abajo.',
 propulsion:'Inicio del tirón y postura sostenida hacia el objetivo. El cuerpo gira hacia el destino en la demostración inferior.',
 release:'Abrir la mano y recuperar la postura en el aire. La animación no frena ni reinicia el impulso al soltarse.',
 stand_up:'Salir del reposo sobre una rodilla: levantar la cadera y recuperar la postura de pie. Mantiene la escala corporal al incorporarse.',
 hurt:'Reacción breve al golpe y recuperación de guardia. Los destellos y la invulnerabilidad pertenecen al juego, no están pintados en el sprite.',
 death:'Pérdida de apoyo, colapso y postura final inmóvil. No contiene sangre; conserva el último fotograma.'
};
const assets=new Map();
for(const item of catalog){item.ready=false;const imgs=item.frames.map(frame=>{const img=new Image();img.src=`${item.id}/${frame.file}`;return img});assets.set(item.id,imgs);Promise.all(imgs.map(img=>img.decode())).then(()=>{item.ready=true;if(selected===item.id)drawSprite()}).catch(()=>{item.error=true;if(selected===item.id)$('frame-label').textContent='No se pudieron cargar los PNG de esta acción.'})}
let selected=catalog[0].id,playing=!matchMedia('(prefers-reduced-motion: reduce)').matches,index=0,elapsed=0,mirrored=false;
let activeGroup='all';
function item(){return catalog.find(a=>a.id===selected)}
function sequence(){const a=item();return a.sequences[$('hit').checked?'hit':'miss']||a.sequences.default}
function actualFrame(){return sequence()[Math.min(index,sequence().length-1)]}
function renderList(){
 const list=$('animation-list');list.replaceChildren();
 for(const a of catalog){if(activeGroup!=='all'&&a.group!==activeGroup)continue;const b=document.createElement('button');b.className='action'+(a.id===selected?' active':'');b.setAttribute('aria-pressed',String(a.id===selected));b.append(document.createTextNode(a.label));const count=document.createElement('span');count.textContent=`${a.frames.length} ${a.frames.length===1?'pose':'poses'}`;b.append(count);b.onclick=()=>select(a.id);list.append(b)}
}
function select(id){selected=id;index=0;elapsed=0;playing=!matchMedia('(prefers-reduced-motion: reduce)').matches;$('hit').checked=false;const a=item();$('action-title').textContent=a.label;$('group-label').textContent=groupNames[a.group];$('count').textContent=`${a.frames.length} ${a.frames.length===1?'pose':'poses'}`;$('hit-option').hidden=!a.sequences.hit;$('action-note').textContent=descriptions[id];$('sheet').src=`${id}/${a.sheet}`;$('sheet-link').href=`${id}/${a.sheet}`;$('json-link').href=`${id}/animation.json`;$('frame').max=sequence().length-1;renderList();drawSprite()}
function drawSprite(){
 const a=item(),canvas=$('sprite-canvas'),ctx=canvas.getContext('2d');ctx.clearRect(0,0,canvas.width,canvas.height);ctx.imageSmoothingEnabled=false;
 if(!a.ready){$('frame-label').textContent=a.error?'Error al cargar PNG.':'Cargando imágenes…';return}
 const n=actualFrame(),meta=a.frames[n],img=assets.get(a.id)[n];const zoom=1.25;const x=(canvas.width-a.frameWidth*zoom)/2,y=(canvas.height-a.frameHeight*zoom)/2;
 ctx.save();ctx.translate(canvas.width/2,0);ctx.scale(mirrored?-1:1,1);ctx.translate(-canvas.width/2,0);ctx.drawImage(img,x,y,a.frameWidth*zoom,a.frameHeight*zoom);
 if($('markers').checked){for(const [key,color]of [['pivot','#ffb74e'],['hand','#ff6ec7'],['hip','#67e6f5']]){const p=meta[key];ctx.strokeStyle=color;ctx.beginPath();ctx.arc(x+p.x*zoom,y+p.y*zoom,4,0,Math.PI*2);ctx.stroke()}}ctx.restore();
 $('frame').value=index;const ending=!playing&&index===sequence().length-1&&!a.loop?' · postura final':'';$('frame-label').textContent=`Pose ${n+1} de ${a.frames.length}${ending}`;$('play').textContent=playing?'Pausa':'Reproducir';
}
$('play').onclick=()=>{if(index===sequence().length-1&&!item().loop&&!playing){index=0;elapsed=0}playing=!playing;drawSprite()};
$('restart').onclick=()=>{index=0;elapsed=0;playing=true;drawSprite()};
$('next').onclick=()=>{playing=false;index=(index+1)%sequence().length;elapsed=0;drawSprite()};
$('frame').oninput=e=>{playing=false;index=Number(e.target.value);elapsed=0;drawSprite()};
$('mirror').onclick=e=>{mirrored=!mirrored;e.target.textContent=mirrored?'Mirar a la derecha':'Mirar a la izquierda';drawSprite()};
$('hit').onchange=()=>{index=0;elapsed=0;playing=true;$('frame').max=sequence().length-1;drawSprite()};$('markers').onchange=drawSprite;
$('filters').onclick=e=>{const b=e.target.closest('[data-group]');if(!b)return;activeGroup=b.dataset.group;document.querySelectorAll('[data-group]').forEach(x=>x.classList.toggle('active',x===b));renderList()};
// Independiente del motor: solo transformación visual de una trayectoria conocida.
let motionTime=0,motionPaused=matchMedia('(prefers-reduced-motion: reduce)').matches,invert=1,lastAngle=0,lastDirection=1;
$('motion-mode').value=motionPaused?'manual':'swing';
$('motion-pause').textContent=motionPaused?'Reanudar trayectoria':'Pausar trayectoria';
$('motion-pause').onclick=e=>{motionPaused=!motionPaused;e.target.textContent=motionPaused?'Reanudar trayectoria':'Pausar trayectoria'};
$('motion-direction').onclick=()=>{invert*=-1;lastDirection*=-1};
$('angle').oninput=()=>{const angle=Number($('angle').value);if(angle!==lastAngle)lastDirection=Math.sign(angle-lastAngle);lastAngle=angle;$('motion-mode').value='manual';drawMotion()};
$('motion-action').onchange=drawMotion;
function transformedDraw(ctx,img,m){ctx.save();ctx.transform(m.a,m.b,m.c,m.d,m.e,m.f);ctx.drawImage(img,0,0);ctx.restore()}
function line(ctx,a,b,color,width){ctx.strokeStyle=color;ctx.lineWidth=width;ctx.beginPath();ctx.moveTo(a.x,a.y);ctx.lineTo(b.x,b.y);ctx.stroke()}
function point(ctx,p,color,r=5){ctx.fillStyle=color;ctx.beginPath();ctx.arc(p.x,p.y,r,0,Math.PI*2);ctx.fill()}
function drawMotion(){
 const canvas=$('motion-canvas'),ctx=canvas.getContext('2d');ctx.clearRect(0,0,760,640);ctx.imageSmoothingEnabled=false;
 const mode=$('motion-mode').value,id=$('motion-action').value,a=catalog.find(x=>x.id===id);if(!a.ready)return;
 let angle=Number($('angle').value),direction=lastDirection;
 if(mode==='swing'){const t=motionTime*0.75*invert;angle=Math.sin(t)*115;direction=Math.sign(Math.cos(t)*invert)||lastDirection}
 if(mode==='orbit'){angle=((motionTime*38*invert+180)%360+360)%360-180;direction=invert}
 if(mode!=='manual'){$('angle').value=Math.round(angle);lastAngle=angle;lastDirection=direction}
 const n=id==='whirlwind'?Math.floor(motionTime*1000/90)%a.frames.length:(id==='propulsion'?a.frames.length-1:0);
 const frame=a.frames[n],img=assets.get(id)[n];
 if(id==='propulsion'){
  const hip={x:380,y:320},r=angle*Math.PI/180,target={x:hip.x+Math.cos(r)*245,y:hip.y+Math.sin(r)*245};
  const solved=SpriteOrientation.propulsion(frame,hip,target);line(ctx,solved.hand,target,'#204b63',5);line(ctx,solved.hand,target,'#67e6f5',2);point(ctx,target,'#ffad5f',6);transformedDraw(ctx,img,solved.matrix);$('motion-status').textContent=`Propulsión hacia el objetivo · ${Math.round(angle)}°`;
 }else{
  const anchor={x:380,y:320},solved=id==='whirlwind'?SpriteOrientation.orbitingBody(frame,anchor,angle,180,direction):SpriteOrientation.pendulum(frame,anchor,angle,92,direction);
  ctx.setLineDash([3,9]);ctx.strokeStyle='#243b4a';ctx.lineWidth=1;ctx.beginPath();ctx.arc(anchor.x,anchor.y,210,0,Math.PI*2);ctx.stroke();ctx.setLineDash([]);
  line(ctx,anchor,solved.hand,'#204b63',5);line(ctx,anchor,solved.hand,'#67e6f5',2);transformedDraw(ctx,img,solved.matrix);point(ctx,anchor,'#ffad5f',6);point(ctx,solved.hand,'#ff76cf',3);
  const hip=solved.hip,arrow={x:hip.x+solved.tangent.x*42,y:hip.y+solved.tangent.y*42};line(ctx,hip,arrow,'#ffad5f',2);point(ctx,arrow,'#ffad5f',3);
  $('motion-status').textContent=`${id==='pendulum'?'Péndulo':'Torbellino'} · ${Math.round(angle)}° · ${Math.abs(angle)>90?'por encima del anclaje':'por debajo del anclaje'} · flecha naranja: sentido del movimiento`;
 }
}
let previous=performance.now();
function tick(now){const dt=Math.min(50,now-previous);previous=now;const a=item();if(playing&&a.ready){const n=actualFrame(),duration=a.frames[n].durationMs;if(duration<=0){playing=false;drawSprite()}else{elapsed+=dt*Number($('speed').value);if(elapsed>=duration){elapsed-=duration;if(index+1<sequence().length)index++;else if(a.loop)index=0;else playing=false;drawSprite()}}}if(!motionPaused)motionTime+=dt/1000;drawMotion();requestAnimationFrame(tick)}
select(selected);requestAnimationFrame(tick);
