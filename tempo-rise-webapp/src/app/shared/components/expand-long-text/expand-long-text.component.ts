import { Component, Input, computed } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { QuillModule } from "ngx-quill";

@Component({
  selector: 'app-expand-long-text',
  standalone: true,
  templateUrl: './expand-long-text.component.html',
  styleUrls: ['./expand-long-text.component.css'],
  imports: [TranslateModule, QuillModule],
})
export class ExpandLongTextComponent {
  @Input() text: string = '';
  @Input() id?: string | number;
  @Input() lengthToShow = 10;

  expanded = new Set<string>();

  isExpanded(): boolean {
    return this.id ? this.expanded.has(this.id.toString()) : false;
  }

  stripHtmlPreserveTags(html: string): string {
  const container = document.createElement('div');
  container.innerHTML = html;

  let currentLength = 0;

  const walk = (node: Node) => {
    if (currentLength >= this.lengthToShow) {
      node.parentNode?.removeChild(node);
      return;
    }

    if (node.nodeType === Node.TEXT_NODE) {
      const text = node.textContent || '';
      const remaining = this.lengthToShow - currentLength;

      if (text.length > remaining) {
        node.textContent = text.substring(0, remaining) + '...';
        currentLength = this.lengthToShow;
      } else {
        currentLength += text.length;
      }
    }

    // clone array because childNodes changes while iterating
    Array.from(node.childNodes).forEach(child => walk(child));
  };

  walk(container);

  return container.innerHTML;
}


  toggleExpand(): void {
    if (!this.id) return;
    const idString = this.id.toString();
    this.expanded.has(idString)
      ? this.expanded.delete(idString)
      : this.expanded.add(idString);
  }

  shouldShowToggle(): boolean {
    const plainText = this.stripHtmlPreserveTags(this.text);
    return plainText.length > this.lengthToShow;
  }

  onToggleClick(event: MouseEvent) {
    event.stopPropagation();
    this.toggleExpand();
  }
}
