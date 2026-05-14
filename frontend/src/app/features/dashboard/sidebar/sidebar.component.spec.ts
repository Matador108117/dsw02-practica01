import { describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { SidebarComponent } from './sidebar.component';

describe('SidebarComponent', () => {
  it('emits entity selection and logout events', () => {
    TestBed.configureTestingModule({
      imports: [SidebarComponent]
    });

    const fixture = TestBed.createComponent(SidebarComponent);
    const component = fixture.componentInstance;
    component.entities = ['empleados', 'departamentos'];

    const selectSpy = vi.fn();
    const logoutSpy = vi.fn();
    component.select.subscribe(selectSpy);
    component.logout.subscribe(logoutSpy);

    fixture.detectChanges();

    const listItems = fixture.nativeElement.querySelectorAll('li');
    listItems[1].click();
    fixture.nativeElement.querySelector('button').click();

    expect(selectSpy).toHaveBeenCalledWith('departamentos');
    expect(logoutSpy).toHaveBeenCalled();
  });
});
